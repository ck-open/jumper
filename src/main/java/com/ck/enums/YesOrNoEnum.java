package com.ck.enums;

import lombok.Getter;

@Getter
public enum YesOrNoEnum implements CodeEnum<String,YesOrNoEnum,String> {

    YES("Y", "是"),
    NO("N", "否");

    private String code;
    private String name;

    YesOrNoEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    /**
     * 获取实例枚举对象
     *
     * @return
     */
    @Override
    public YesOrNoEnum get() {
        return this;
    }
}
