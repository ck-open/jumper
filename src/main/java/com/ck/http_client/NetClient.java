package com.ck.http_client;



import com.ck.http_client.netenum.ContentTypeEnum;
import com.ck.http_client.netenum.RequestMethodEnum;
import javafx.util.Pair;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网络连接请求客户端
 *
 * @author cyk
 * @since 2020-01-01
 */
public class NetClient {
    public static NetClient GET(String url) {
        return new NetClient(url, RequestMethodEnum.GET);
    }

    public static NetClient POST(String url) {
        return new NetClient(url, RequestMethodEnum.POST);
    }

    public static NetClient PUT(String url) {
        return new NetClient(url, RequestMethodEnum.PUT);
    }

    public static NetClient DELETE(String url) {
        return new NetClient(url, RequestMethodEnum.DELETE);
    }


    /**
     * 目标地址
     */
    private String url;

    /**
     * 请求方式
     */
    private RequestMethodEnum method;

    /**
     * 参数列表
     */
    private Map<String, List<String>> parameter;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求主体正文
     */
    private String content;


    private NetClient(String url, RequestMethodEnum method) {
        this.url = url;
        this.method = method;
    }


    /**
     * 发送请求并解读返回结果
     *
     * @return
     */
    public NetUtil.NetResult execute(){
        return execute( 60000);
    }
    /**
     * 发送请求并解读返回结果
     *
     * @return
     */
    public NetUtil.NetResult execute( int readTimeout) {
        return execute(60000,readTimeout);
    }
    /**
     * 发送请求并解读返回结果
     *
     * @return
     */
    public NetUtil.NetResult execute(int timeout, int readTimeout) {

        // 创建连接对象
        HttpURLConnection httpURLConnection = NetUtil.getConnection(this.url, this.method.getMethod(), this.parameter, this.headers,timeout,readTimeout);

        // 有json数据
        if (this.content != null)
            NetUtil.sendContent(httpURLConnection, this.content);

        // 解读响应结果
        NetUtil.NetResult netResult = NetUtil.getNetResult(httpURLConnection);

        return netResult;
    }


    public String getUrl() {
        return url;
    }

    public NetClient setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeaderValue(String key) {
        if (headers == null) return null;
        return headers.get(key);
    }

    public NetClient setHeaders(String key, String val) {
        setHeaders(new Pair<>(key, val));
        return this;
    }

    public NetClient setHeaders(Pair<String, String>... headers) {
        if (headers != null) {
            Map<String, String> head = new HashMap<>();
            Arrays.stream(headers).forEach(item -> head.put(item.getKey(), item.getValue()));
            setHeaders(head);
        }
        return this;
    }

    public NetClient setHeaders(Map<String, String> headers) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        return this;
    }

    public Map<String, List<String>> getParameter() {
        return parameter;
    }

    public NetClient setParameter(String key, String... parameter) {
        if (parameter != null) {
            setParameter(key, Arrays.asList(parameter));
        }
        return this;
    }

    public NetClient setParameter(Map<String, List<String>> parameter) {
        if (parameter != null) {
            parameter.forEach((k, v) -> setParameter(k, v));
        }
        return this;
    }


    public NetClient setParameter(Pair<String, List<String>>... parameter) {
        if (parameter != null) {
            Arrays.stream(parameter).forEach(i -> setParameter(i.getKey(), i.getValue()));
        }
        return this;
    }

    public NetClient setParameter(String key, List<String> parameter) {
        if (this.getParameter() == null) {
            this.parameter = new HashMap<>();
        }
        if (!this.parameter.containsKey(key)) {
            this.parameter.put(key, parameter);
        } else {
            this.parameter.get(key).addAll(parameter);
        }
        return this;
    }


    public String getContent() {
        return content;
    }

    public NetClient setContent(String content) {
        if (RequestMethodEnum.GET.equals(this.method)) {
            throw new RuntimeException("GET requests do not allow content；  GET 请求方式不能设置Content");
        } else if (RequestMethodEnum.DELETE.equals(this.method)) {
            throw new RuntimeException("DELETE requests do not allow content；  DELETE 请求方式不能设置Content");
        }
        this.content = content;
        return this;
    }


    /**
     * 设置请求头 Content-Type
     *
     * @param contentType
     * @return
     */
    public NetClient setContentType(ContentTypeEnum contentType) {
        return this.setHeaders("Content-Type", contentType.getType());
    }

    /**
     * 设置请求头 Content-Length
     *
     * @param length Bytes().length
     * @return
     */
    public NetClient setContentLength(int length) {
        return this.setHeaders("Content-Length", String.valueOf(length));
    }

    /**
     * 设置请求头 Content-Type = Application_Json
     *
     * @return
     */
    public NetClient setContentType_Application_Json() {
        return this.setContentType(ContentTypeEnum.Application_Json);
    }

    /**
     * 设置请求头 Content-Type = Application_Octet_Stream
     *
     * @return
     */
    public NetClient setContentType_Application_Octet_Stream() {
        return this.setContentType(ContentTypeEnum.Application_Octet_Stream);
    }

    /**
     * 设置请求头 Content-Type = Application_Xml
     *
     * @return
     */
    public NetClient setContentType_Application_Xml() {
        return this.setContentType(ContentTypeEnum.Application_Xml);
    }

    /**
     * 设置请求头 Content-Type = Multipart_Form_Data
     *
     * @return
     */
    public NetClient setContentType_Multipart_Form_Data() {
        return this.setContentType(ContentTypeEnum.Multipart_Form_Data);
    }


}
