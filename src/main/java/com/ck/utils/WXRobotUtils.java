package com.ck.utils;

import com.alibaba.fastjson.JSONObject;
import com.ck.http_client.NetUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 腾讯企业微信群消息机器人
 * 官方API文档：https://developer.work.weixin.qq.com/document/path/91770
 *
 * @author cyk
 * @since 2022-01-11
 */
public class WXRobotUtils {
    private static Logger log = Logger.getLogger(WXRobotUtils.class.getName());
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);
    private static final String robot_url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send";
    private static final String robot_url_file = "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media";


    /**
     * 发送消息 - 异步
     *
     * @param key                 群机器人创建时的key
     * @param content             消息文本内容
     * @param uuid                消息ID
     * @param mentionedMobileList 需要 @ 那些手机号的人
     */
    public static void sendTextAsync(String key, String content, String uuid, String... mentionedMobileList) {
        THREAD_POOL.execute(() -> sendText(key, content, uuid, mentionedMobileList));
    }


    /**
     * 发送文件 - 异步
     *
     * @param key      群机器人创建时的key
     * @param fileName 文件名称 test.txt
     * @param file     文件
     */
    public static void sendFileAsync(String key, String fileName, File file) {
        THREAD_POOL.execute(() -> sendFile(key, fileName, file));
    }

    /**
     * 发送文件 - 异步
     *
     * @param key      群机器人创建时的key
     * @param fileName 文件名称 test.txt
     * @param file     文件二进制内容
     */
    public static void sendFileAsync(String key, String fileName, byte[] file) {
        THREAD_POOL.execute(() -> sendFile(key, fileName, file));
    }

    /**
     * 发送图片 - 异步
     *
     * @param key       群机器人创建时的key
     * @param imageFile 文件
     */
    public static void sendImageAsync(String key, File imageFile) {
        THREAD_POOL.execute(() -> sendImage(key, imageFile));
    }

    /**
     * 发送图片 - 异步
     *
     * @param key       群机器人创建时的key
     * @param imageFile 文件二进制内容
     */
    public static void sendImageAsync(String key, byte[] imageFile) {
        THREAD_POOL.execute(() -> sendImage(key, imageFile));
    }

    /**
     * 发送消息
     *
     * @param key                 群机器人创建时的key
     * @param content             消息文本内容
     * @param uuid                消息ID
     * @param mentionedMobileList 需要 @ 那些手机号的人
     * @return 返回消息中的uuid
     */
    public static String sendText(String key, String content, String uuid, String... mentionedMobileList) {
        return sendMessage(key, "text", content, uuid, null, mentionedMobileList);
    }


    /**
     * 发送文件
     *
     * @param key      群机器人创建时的key
     * @param fileName 文件名称 test.txt
     * @param file     文件
     * @return 腾讯响应结果
     */
    public static String sendFile(String key, String fileName, File file) {
        byte[] data = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            NetUtil.io(new FileInputStream(file), byteArrayOutputStream);
            data = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.warning("发送WX文件解析异常：" + e.getMessage());
            return "文件解析异常";
        }
        return sendFile(key, fileName, data);
    }

    /**
     * 发送文件
     *
     * @param key      群机器人创建时的key
     * @param fileName 文件名称 test.txt
     * @param file     文件二进制内容
     * @return 腾讯响应结果
     */
    public static String sendFile(String key, String fileName, byte[] file) {
        return sendMessage(key, "file", uploadingFile(key, fileName, file), null, null);
    }

    /**
     * 发送图片
     *
     * @param key       群机器人创建时的key
     * @param imageFile 文件
     * @return 腾讯响应结果
     */
    public static String sendImage(String key, File imageFile) {
        // 图片内容（base64编码前）的md5值
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            NetUtil.io(new FileInputStream(imageFile), byteArrayOutputStream);
        } catch (IOException e) {
            log.warning("发送WX图片 MD5 和 Base64 转换异常：" + e.getMessage());
        }
        return sendImage(key, byteArrayOutputStream.toByteArray());
    }

    /**
     * 发送图片
     *
     * @param key       群机器人创建时的key
     * @param imageFile 文件二进制内容
     * @return 腾讯响应结果
     */
    public static String sendImage(String key, byte[] imageFile) {
        String md5 = null;
        String body = null;
        try {
            body = Base64.getEncoder().encodeToString(imageFile);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(imageFile);
            md5 = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            log.warning("发送WX图片 MD5 和 Base64 转换异常：" + e.getMessage());
        }

        String finalBody = body;
        String finalMd = md5;
        return sendMessage(key, "image", finalBody, finalMd, null);
    }

    /**
     * 发送消息
     *
     * @param key                 群机器人创建时的key
     * @param msgType             消息类型：markdown、image、file、text（默认）
     * @param content             消息文本内容  (发送文件时上传后返回的 media_id)
     * @param md5                 发图片时 图片内容（base64编码前）的md5值 （content 图片文件 base64 加密后字符串）
     * @param mentionedList       需要 @ 那些账号
     * @param mentionedMobileList 需要 @ 那些手机号的人
     * @return 腾讯响应结果
     */
    public static String sendMessage(String key, String msgType, String content, String md5, List<String> mentionedList, String... mentionedMobileList) {

        try {


            String body = "";
            if ("markdown".equals(msgType)) {
            /* markdown类型 消息    报文如下
                    msgtype	是	消息类型，此时固定为image
                    base64	是	图片内容的base64编码
                    md5	是	图片内容（base64编码前）的md5值
                    注：图片（base64编码前）最大不能超过2M，支持JPG,PNG格式
                    {
                        "msgtype": "markdown",
                        "markdown": {
                            "content": "实时新增用户反馈<font color=\"warning\">132例</font>，请相关同事注意。\n
                             >类型:<font color=\"comment\">用户反馈</font>
                             >普通用户反馈:<font color=\"comment\">117例</font>
                             >VIP用户反馈:<font color=\"comment\">15例</font>"
                        }
                    }
             */
                body = String.format("{\"msgtype\":\"markdown\",\"markdown\":{\"content\":\"%s\"}}", content);
            } else if ("image".equals(msgType)) {
            /* image类型 消息   报文如下
                msgtype	是	消息类型，此时固定为image
                base64	是	图片内容的base64编码
                md5	是	图片内容（base64编码前）的md5值
                注：图片（base64编码前）最大不能超过2M，支持JPG,PNG格式
                    {
                        "msgtype": "image",
                        "image": {
                            "base64": "DATA",
                            "md5": "MD5"
                        }
                    }
             */
                body = String.format("{\"msgtype\":\"image\",\"image\":{\"base64\":\"%s\",\"md5\":\"%s\"}}", content, md5);
            } else if ("file".equals(msgType)) {
            /* file类型 消息  报文如下
                    {
                        "msgtype": "file",
                        "file": {
                            "media_id": "3a8asd892asd8asd"
                        }
                    }
             */
                String template = "{\"msgtype\":\"file\",\"file\":{\"media_id\":\"%s\"}}";
                body = String.format(template, content);
            } else {
            /* 文本消息   报文如下
                    {
                        "msgtype": "text",
                        "text": {
                            "content": "广州今日天气：29度，大部分多云，降雨概率：60%",
                            "mentioned_list":["wangqing","@all"],
                            "mentioned_mobile_list":["13800001111","@all"]
                        }
                    }
             */
                String template = "{\"msgtype\":\"text\",\"text\":{\"content\":\"%s\",\"mentioned_list\":[%s],\"mentioned_mobile_list\":[%s]}}\n";
                String mentionedStr = "";
                if (mentionedList != null) {
                    StringBuffer msb = new StringBuffer();
                    for (String m : mentionedList) {
                        msb.append(",\"").append(m).append("\"");
                    }
                    if (msb.toString().startsWith(",")) {
                        mentionedStr = msb.toString().substring(1);
                    }
                }

                String mentionedMobileStr = "";
                if (mentionedMobileList != null) {
                    StringBuffer msb = new StringBuffer();
                    for (String m : mentionedMobileList) {
                        msb.append(",\"").append(m).append("\"");
                    }
                    if (msb.toString().startsWith(",")) {
                        mentionedMobileStr = msb.toString().substring(1);
                    }
                }
                body = String.format(template, content, mentionedStr, mentionedMobileStr);
            }


            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept-Charset", "utf-8");

            Map<String, List<String>> parameter = new HashMap<>();
            parameter.put("key", Collections.singletonList(key));

            HttpURLConnection connection = NetUtil.getConnectionPost(robot_url, parameter, headers);
            if (NetUtil.sendContent(connection, body)) {
                NetUtil.NetResult result = NetUtil.getNetResult(connection);
                JSONObject response = JSONObject.parseObject(result.getContent());
                if (200 == result.getResponseCode() && 0 == response.getInteger("errcode")) {
                    return result.getContent();
                } else {
                    log.warning("发送WX消息失败：" + response.toJSONString());
                }
            }
            NetUtil.close(connection);
        } catch (Exception e) {
            log.warning("发送WX消息异常：" + e);
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上传文件
     *
     * @param key      群机器人创建时的key
     * @param fileName 文件名称 test.txt
     * @param file     文件二进制内容
     * @return media_id
     */
    public static String uploadingFile(String key, String fileName, byte[] file) {
        /*
            Content-Disposition: form-data; name="media";filename="wework.txt"; filelength=6
            Content-Type: application/octet-stream

            https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=8e281ea3-37fc-46f8-b351-8e4e391c3487
         */
        try {

            if (file != null) {

                // form-data 请求报文体前缀
                byte[] formDataPrefix = String.format("---------------------------acebdf13572468\r\n" +
                        "Content-Disposition: form-data; name=\"media\";filename=\"%s\"; filelength=%s\r\n" +
                        "Content-Type: application/octet-stream\r\n\r\n", fileName, file.length).getBytes(StandardCharsets.UTF_8);

                // form-data 请求报文体后缀（结束标记）
                byte[] formDataSuffix = "\r\n---------------------------acebdf13572468--\r\n".getBytes(StandardCharsets.UTF_8);

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", " multipart/form-data; boundary=-------------------------acebdf13572468");
                headers.put("Content-Length", String.valueOf(file.length + formDataPrefix.length + formDataSuffix.length));

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("type", Collections.singletonList("file"));
                parameter.put("key", Collections.singletonList(key));

                HttpURLConnection connection = NetUtil.getConnectionPost(robot_url_file, parameter, headers);
                connection.connect();
                OutputStream out = connection.getOutputStream();
                NetUtil.io(new ByteArrayInputStream(formDataPrefix), out);
                NetUtil.io(new ByteArrayInputStream(file), out);
                NetUtil.io(new ByteArrayInputStream(formDataSuffix), out);
                NetUtil.closeIO(null, out);
                NetUtil.NetResult result = NetUtil.getNetResult(connection);
                NetUtil.close(connection);
                if (200 == result.getResponseCode()) {
                    /*
                    正确响应报文：
                            {
                               "errcode": 0,
                               "errmsg": "ok",
                               "type": "file",
                               "media_id": "1G6nrLmr5EC3MMb_-zK1dDdzmd0p7cNliYu9V5w7o8K0",
                               "created_at": "1380000000"
                            }
                     */
                    JSONObject response = JSONObject.parseObject(result.getContent());
                    if (0 == response.getInteger("errcode")) {
                        return response.getString("media_id");
                    } else {
                        log.warning("文件上传失败：" + response.toJSONString());
                    }
                }
            }
        } catch (IOException e) {
            log.warning("发送WX文件上传异常：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异常数据保存到文件 并 发送企业微信通知
     *
     * @param sendKey       密钥
     * @param sendContent   企业微信消息文本内容
     * @param e             异常对象
     * @param packagePrefix 包前缀，异常堆栈信息中符合前缀的信息将打印到消息面板中，null则不打印
     */
    public static void sendError(String sendKey, String sendContent, Exception e, String debugData, String... packagePrefix) {
        sendErrorAndExport(sendKey, sendContent, e, debugData, null, packagePrefix);
    }

    /**
     * 异常数据保存到文件 并 发送企业微信通知
     *
     * @param sendKey       密钥
     * @param sendContent   企业微信消息文本内容
     * @param e             异常对象
     * @param debugData     发生异常的数据
     * @param exportPath    异常文件储存地址  null则不储存
     * @param packagePrefix 包前缀，异常堆栈信息中符合前缀的信息将打印到消息面板中，null则不打印
     */
    public static void sendErrorAndExport(String sendKey, String sendContent, Exception e, String debugData, String exportPath, String... packagePrefix) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        // 异常日志发送企业微信
//        sendContent = sendContent + "\r" + " Time：" + simpleDateFormat.format(new Date()) + "\r异常文件：" + errorExportPath + "\r堆栈信息：\r" + ExceptionUtil.getExceptionStackTraceInfo(e,3,"cn.com.hyundai") + "\r";
        sendContent = "Msg：" + sendContent + "\r" + "Time：" + simpleDateFormat.format(new Date()) + "\rUUID："+uuid+"\r";

        if (e != null) {
            if (exportPath != null && !"".equals(exportPath.trim())) {
                // 异常日志保存到本地
                exportPath = ExceptionUtil.exceptionStackTraceExportToFile(e, debugData, uuid, exportPath);
                sendContent += "异常文件：" + exportPath + "\r";
            }
            if (packagePrefix != null) {
                sendContent += "StackTraceInfo：\r" + ExceptionUtil.getExceptionStackTraceInfo(e, 5, packagePrefix) + "\r";
            }
            debugData += "\r\n\r\n" + ExceptionUtil.getExceptionStackTraceInfo(e);
        }
        WXRobotUtils.sendTextAsync(sendKey, sendContent, uuid);
        if (debugData != null && !"".equals(debugData.trim()))
            WXRobotUtils.sendFileAsync(sendKey, uuid + ".txt", debugData.getBytes(StandardCharsets.UTF_8));
    }


    public static void main(String[] args) {

    }

    private static void sendRobotTest() {
        String robot_key = "626f61c3-78ca-4d0f-b979-533276196599";
//        sendFile(robot_key, "123.txt", "测试文档内容fghfghxdzxfghkfhiyuhuo;jijhiugyufgxzawdsARZefgdtfgjhgigytufhgfchjgjhgchfcgnhgfxf".getBytes(StandardCharsets.UTF_8));

        sendImage(robot_key, new File("D:\\图片\\0017031094904213_b.jpg"));
//        sendImage(robot_key, new File("D:\\WBS2021-12-01.png"));

//        sendText(robot_key,"测试信息");
    }

    private static void sendErrorTest() {
        String sendKey = "8b85f250-8798-44de-abc0-bfe9dbff2c07";
        String errorExportPath = "/home/tomcat/DataLogs/";

        try {
            String str = "";
            str.substring(0, 10);
        } catch (Exception e) {
            System.out.println(ExceptionUtil.getExceptionStackTraceInfo(e));
            sendErrorAndExport(sendKey, "本地测试微信异常日志(" + NetUtil.getHostAddress() + ")\r", e, "异常附带文件信息", errorExportPath);
        }
    }

}
