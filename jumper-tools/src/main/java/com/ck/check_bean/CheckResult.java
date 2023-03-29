package com.ck.check_bean;

import java.lang.reflect.Field;

/**
 * @ClassName CheckValue
 * @Description 检查校验结果集
 * @Author Cyk
 * @Version 1.0
 * @since 2022/8/5 9:44
 **/
public class CheckResult {
    /**
     * 是否成功
     */
    private boolean isSucceed = false;
    /**
     * 校验的类
     */
    private Class cla;
    /**
     * 校验的字段
     */
    private Field field;
    /**
     * 校验的失败信息
     */
    private String message;
    /**
     * 配置的正则
     */
    private String regexp;
    /**
     * 配置的最大值
     */
    private Double max;
    /**
     * 配置的最小值
     */
    private Double min;
    /**
     * 校验的值
     */
    private Object value;

    public Class getCla() {
        return cla;
    }

    public CheckResult setCla(Class cla) {
        this.cla = cla;
        return this;
    }

    public Field getField() {
        return field;
    }

    public CheckResult setField(Field field) {
        this.field = field;
        return this;
    }

    public String getRegexp() {
        return regexp;
    }

    public CheckResult setRegexp(String regexp) {
        this.regexp = regexp;
        return this;
    }

    public Double getMax() {
        return max;
    }

    public CheckResult setMax(Double max) {
        this.max = max;
        return this;
    }

    public Double getMin() {
        return min;
    }

    public CheckResult setMin(Double min) {
        this.min = min;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CheckResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public CheckResult setSucceed(boolean succeed) {
        isSucceed = succeed;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public CheckResult setValue(Object value) {
        this.value = value;
        return this;
    }
}
