package com.ck.core.utils;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@Slf4j
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextUtil.applicationContext == null) {
            SpringContextUtil.applicationContext = applicationContext;
        }
        log.info("ApplicationContext配置成功,applicationContext对象：" + SpringContextUtil.applicationContext);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public static <T> LambdaQueryChainWrapper<T> getLambdaQuery(String name) {
        try {
            return new LambdaQueryChainWrapper<T>(getBaseMapper(name));
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> BaseMapper<T> getBaseMapper(String name) {
        try {
            if (!name.endsWith("Mapper")) name += "Mapper";
            return getBean(name, BaseMapper.class);
        } catch (Exception e) {
            try {
                return getBean(name.replace("Mapper", "Dao"), BaseMapper.class);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
