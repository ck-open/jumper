package com.ck.enums;

/**
 * 所有枚举的父接口
 */
public interface CodeEnum<C, T extends Enum<T> & CodeEnum<C, T, V>, V> {

    C getCode();

    String getName();

    V getValue();

}