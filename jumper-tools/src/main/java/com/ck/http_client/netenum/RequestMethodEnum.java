package com.ck.http_client.netenum;

/**
 * 请求方式枚举
 *
 * @author cyk
 * @since 2020-01-01
 */
public enum RequestMethodEnum {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    private String method;

    RequestMethodEnum(String method){
        this.method = method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
