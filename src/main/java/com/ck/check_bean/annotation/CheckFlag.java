package com.ck.check_bean.annotation;

import java.lang.annotation.*;

/**
 * @author Cyk
 * @description 参数检查注解
 * @since 15:46 2022/8/2
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CheckFlag {
    /**
     * 检查Flag选项
     *
     * @return
     */
    String value() default "";
}