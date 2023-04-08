package com.ck.core.mybatis;

/**
 * Po 转换 Dto 接口
 * 公共 controller 中使用时 需要在各自服务中注入实现bean
 */
public interface IConvert<T, R> {
    T convert(R val);
}
