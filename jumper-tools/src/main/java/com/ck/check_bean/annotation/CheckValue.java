package com.ck.check_bean.annotation;

import java.lang.annotation.*;

/**
 * @author Cyk
 * @description 参数检查注解
 * @since 15:46 2022/8/2
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface CheckValue {
    /**
     * 检查失败提示信息
     *
     * @return
     */
    String value() default "";

    /**
     * 属性名称
     *
     * @return
     */
    String name() default "";

    /**
     * 最大值
     *
     * @return
     */
    double max() default -999999999;

    /**
     * 最小值
     *
     * @return
     */
    double min() default -999999999;

    /**
     * 为空时的默认值
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * 参数正则检查
     *
     * @return
     */
    String regexp() default "";

    /**
     * 是否需要子级检查
     *
     * @return
     */
    boolean isChild() default false;

    /**
     * 是否非必传参数
     *
     * @return
     */
    boolean isOptional() default false;

    /**
     * 自定义是否校验标签列表
     * 配合 isOptional() 使用  为true 时 本列表包含指定的校验标记则进行校验非空
     *
     * @return
     */
    String[] flag() default {};
}