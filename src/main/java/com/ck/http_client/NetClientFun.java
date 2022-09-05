package com.ck.http_client;


import com.ck.function.FunctionUtil;

import java.util.Map;

/**
 * 网络连接请求客户端-函数支持
 *
 * @author cyk
 * @since 2021-10-01
 */
public class NetClientFun {
    private NetClient client;

    private NetClientFun(NetClient client) {
        this.client = client;
    }

    public static NetClientFun GET(String url) {
        return new NetClientFun(NetClient.GET(url));
    }

    public static NetClientFun POST(String url) {
        return new NetClientFun(NetClient.POST(url));
    }

    public static NetClientFun PUT(String url) {
        return new NetClientFun(NetClient.PUT(url));
    }

    public static NetClientFun DELETE(String url) {
        return new NetClientFun(NetClient.DELETE(url));
    }

    public String getUrl() {
        return this.client.getUrl();
    }

    /**
     * 设置请求目标地址
     *
     * @param url
     * @return
     */
    public NetClientFun setUrl(String url) {
        this.client.setUrl(url);
        return this;
    }

    /**
     * 获取请求头
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return this.client.getHeaders();
    }

    /**
     * 设置请求头
     *
     * @param func
     * @return
     */
    public <T, S> String getHeaderValue(FunctionUtil.Func<? super T, ? extends S> func) {
        return this.client.getHeaderValue(FunctionUtil.getFieldName(func));
    }

    /**
     * 设置请求头
     *
     * @param func
     * @param val
     * @return
     */
    public <T, S> NetClientFun setHeaders(FunctionUtil.Func<? super T, ? extends S> func, T val) {
        this.client.setHeaders(FunctionUtil.getFieldName(func), func.apply(val).toString());
        return this;
    }

    /**
     * 设置请求头
     *
     * @param func
     * @param val
     * @return
     */
    public <T, S> NetClientFun getParameter(FunctionUtil.Func<? super T, ? extends S> func, T val) {
        this.client.setParameter(FunctionUtil.getFieldName(func), func.apply(val).toString());
        return this;
    }


    /**
     * 设置请求正文
     *
     * @param func
     * @param val
     * @return
     */
    public <T, S> NetClientFun setContent(FunctionUtil.Func<? super T, ? extends S> func, T val) {
        this.setContent(func.apply(val).toString());
        return this;
    }

    /**
     * 设置请求正文
     *
     * @param content
     * @return
     */
    public NetClientFun setContent(String content) {
        this.client.setContent(content);
        return this;
    }

    public String getContent() {
        return this.client.getContent();
    }

    /**
     * 获取客户端请求对象
     * @return
     */
    public NetClient getClient() {
        return this.client;
    }

    public static void main(String[] args) {
        NetClientFun clientFun = NetClientFun.GET("sub");
        clientFun.setHeaders(NetUtil.NetResult::getContent, new NetUtil.NetResult().setContent("hjgjhgjgjk"));
        System.out.println(clientFun.getContent());
    }
}
