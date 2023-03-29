package com.ck.core.feign;

import org.springframework.beans.BeansException;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态创建FeignClient 工具类
 */
public class FeignClientUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;
    private static final Map<String, Object> BEAN_CACHE = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (FeignClientUtils.applicationContext == null) {
            FeignClientUtils.applicationContext = applicationContext;
        }
    }

    public static <T> T build(String serverName, String url, Class<T> targetClass) {
        return buildClient(serverName, url, targetClass, null);
    }

    public static <T> T build(String serverName, Class<T> targetClass) {
        return buildClient(serverName, null, targetClass, null);
    }

    public static <T> T build(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
        return buildClient(serverName, url, targetClass, fallback);
    }

    public static <T> T build(String serverName, Class<T> targetClass, Class<? extends T> fallback) {
        return buildClient(serverName, null, targetClass, fallback);
    }

    @SuppressWarnings("unchecked")
    private static <T> T buildClient(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
        T t = (T) BEAN_CACHE.get(serverName);
        if (Objects.isNull(t)) {
            FeignClientBuilder.Builder<T> builder = new FeignClientBuilder(applicationContext).forType(targetClass, serverName).url(url).fallback(fallback);
            t = builder.build();
            BEAN_CACHE.put(serverName, t);
        }
        return t;
    }
}
