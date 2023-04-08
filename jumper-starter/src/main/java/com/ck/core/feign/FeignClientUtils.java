package com.ck.core.feign;

import com.ck.core.utils.SpringContextUtil;
import org.springframework.cloud.openfeign.FeignClientBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态创建FeignClient 工具类
 */
public class FeignClientUtils {

    private static final Map<String, Object> BEAN_CACHE = new ConcurrentHashMap<>();

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
            FeignClientBuilder.Builder<T> builder = new FeignClientBuilder(SpringContextUtil.getApplicationContext()).forType(targetClass, serverName).url(url).fallback(fallback);
            t = builder.build();
            BEAN_CACHE.put(serverName, t);
        }
        return t;
    }
}
