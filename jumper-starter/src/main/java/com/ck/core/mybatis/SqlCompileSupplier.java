package com.ck.core.mybatis;

import java.util.Map;

/**
 * @ClassName SqlCompileSupplier
 * @Description 提供需要编译成 BaseMapper 对象的Sql 列表 将在服务启动后执行
 * @Author Cyk
 * @Version 1.0
 * @since 2023/1/30 13:37
 **/
public interface SqlCompileSupplier {

    /**
     * 需要编译的 Sql 列表
     * key: beanName  value: Sql语句
     *
     * @return
     */
    Map<String, String> getSql();

}
