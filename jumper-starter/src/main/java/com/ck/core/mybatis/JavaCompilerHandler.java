package com.ck.core.mybatis;

import java.util.Map;

/**
 * java 源码编译执行器
 */
public interface JavaCompilerHandler {
    Map<String, Class<?>> compiler(String javaCode);
}
