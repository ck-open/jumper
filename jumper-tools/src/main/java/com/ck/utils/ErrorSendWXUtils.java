package com.ck.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AsyncAppenderBase;
import com.ck.http_client.NetUtil;
import lombok.Data;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * 异常日志发送企业微信
 * 注意在logback.xml中配置该类
 * <p>
 *      <!--yml 配置文件logging.wx_log.url 来配置企业微信消息发送服务地址-->
 *     <springProperty name="wx_log_url" scope="context" source="logging.wx_log.url" defaultValue="https://tiger-test.hinsurance.cn/tiger-base/ApiLog/sendWXMsg"/>
 *     <!--yml 配置文件logging.wx_log.sendTag 来配置消息发送关键字-->
 *     <springProperty name="wx_log_sendTag" scope="context" source="logging.wx_log.sendTag" defaultValue="WX"/>
 *
 *     <appender name="wx_log_async" class="cn.com.hyundai.tiger.common.utils.ErrorSendWXUtils">
 *         <!--企业微信消息发送服务地址-->
 *         <wxMsgUrl>${wx_log_url}</wxMsgUrl>
 *         <sendTag>${wx_log_sendTag}</sendTag>
 *         <discardingThreshold>0</discardingThreshold>
 *         <appender-ref ref="wx_log_async"/>
 *     </appender>
 *
 *     <root level="info">
 *          <appender-ref ref="wx_log_async"/>
 *     </root>
 *
 * @author cyk
 * @since 2022-01-01
 */
@Data
public class ErrorSendWXUtils extends AsyncAppenderBase<ILoggingEvent> {
    private static final Logger log = Logger.getLogger(ErrorSendWXUtils.class.getName());
    /**
     * 日志中带有此标记的则发送微信消息
     */
    public static final Marker SEND_WX_MESSAGE = MarkerFactory.getMarker("SEND_WX_MESSAGE");

    /**
     * 微信服务API地址
     */
    private String wxMsgUrl;


    /**
     * 微信消息发送标签（默认WX）
     */
    private String sendTag;

    /**
     * 所有Error 级别日志 并且 Message以 sendTag 字符结尾 或 带有 SEND_WX_MESSAGE 标记 的日志则发送微信消息
     * <p>
     * 消息包括：服务IP、服务名称、Message、日志打印所在类、异常抛出所在类、堆栈信息（3条）
     *
     * @param eventObject
     */
    @Override
    protected void append(ILoggingEvent eventObject) {
        if (Level.ERROR.equals(eventObject.getLevel())) {
            // 不满足发送消息条件则结束方法
            if (!(sendTag != null && eventObject.getMessage().contains(sendTag))
                    && !(eventObject.getMarker() != null && SEND_WX_MESSAGE.getName().equals(eventObject.getMarker().getName()))) {
                return;
            }

            StringBuilder msg = new StringBuilder();
            try {
                String id = "";
                String ip = null;
                LoggerContextVO vo = eventObject.getLoggerContextVO();
                if (vo != null && vo.getPropertyMap() != null) {
                    ip = vo.getPropertyMap().get("ServerIP");
                    msg.append("\r\nAppName：").append(vo.getPropertyMap().get("APP_NAME"));
                }
                if (ip == null) ip = NetUtil.getOuterIp();

                if (eventObject.getMDCPropertyMap() != null && eventObject.getMDCPropertyMap().containsKey("requestId"))
                    id = eventObject.getMDCPropertyMap().get("requestId");

                msg.append("\r\nMessage：").append(eventObject.getMessage());
                msg.append("\r\nLogClass：").append(eventObject.getLoggerName());
                if (eventObject.getThrowableProxy() != null) {
                    msg.append("\r\nErrorMessage：").append(eventObject.getThrowableProxy().getMessage());
                    msg.append("\r\nErrorClass：").append(eventObject.getThrowableProxy().getClassName());
                    if (eventObject.getThrowableProxy().getStackTraceElementProxyArray() != null) {

                        int index = 0;
                        for (StackTraceElementProxy i : eventObject.getThrowableProxy().getStackTraceElementProxyArray()) {
                            String stTemp = i.getSTEAsString();
                            if (stTemp.startsWith("at cn.com.hyundai") && !stTemp.contains("$")) {
                                if (index > 2) break;
                                index++;
                                msg.append("\r\n\b->").append(stTemp);
                            }
                        }
                    }
                }

                sendWXMsg(id, msg.toString(), ip);
            } catch (Exception e) {
                // 忽略异常
                log.warning("logback Error Log 发送企业微信通知异常：\r\n" + msg.toString());
            }
        }
    }

    /**
     * 发送微信消息
     *
     * @param id
     * @param content
     * @param ip
     * @throws IOException
     */
    private void sendWXMsg(String id, String content, String ip) throws IOException {
        Map<String, String> headers = new HashMap();

        Map<String, List<String>> parameter = new HashMap();
        parameter.put("id", Collections.singletonList(id));
        parameter.put("content", Collections.singletonList(content));
        parameter.put("requestIP", Collections.singletonList(ip));
//        HttpURLConnection conn = NetUtil.getConnectionPost("https://tiger-test.hinsurance.cn/tiger-base/ApiLog/sendWXMsg", parameter, headers);
        HttpURLConnection conn = NetUtil.getConnectionPost(wxMsgUrl, parameter, headers);
        conn.connect();
        NetUtil.NetResult result = NetUtil.getNetResult(conn);
        NetUtil.close(conn);
    }
}
