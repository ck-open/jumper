package com.ck.enums;

/**
 * 所有枚举的父接口
 */
public interface CodeEnum<C, T extends Enum<T> & CodeEnum<C, T, V>, V> {

    /**
     * 获取枚举代码
     * @return
     */
    C getCode();

    /**
     * 获取枚举名称
     * @return
     */
    String getName();

    /**
     * 获取枚举值
     * @return
     */
    V getValue();

    /**
     * 获取实例枚举对象
     * @return
     */
    T get();

}
