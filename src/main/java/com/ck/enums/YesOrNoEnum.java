package com.ck.enums;

import lombok.Getter;

@Getter
public enum YesOrNoEnum implements CodeEnum {

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
    public Object getValue() {
        return this.code;
    }
}
