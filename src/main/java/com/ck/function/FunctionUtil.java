
package com.ck.function;

import com.ck.function.serializable.SerializedLambda;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Function 工具类
 *
 * @author cyk
 * @since 2021-10-01
 */
public final class FunctionUtil implements Serializable {

    /**
     * 根据当前线程缓存SerializedLambda对象
     */
    private static ThreadLocal<Map<String, SerializedLambda>> serializedLambdaThreadLocal = new ThreadLocal<>();


    /**
     * 通过反序列化转换 Function 函数表达式为SerializedLambda 对象并缓存到当前线程中
     *
     * @param func Function 函数对象
     * @return 返回解析后的 SerializedLambda
     */
    public static <T extends Function & Serializable> SerializedLambda resolveSLambda(T func) {
        if (!func.getClass().isSynthetic()) {
            throw new RuntimeException("该方法仅能传入  Lambda::getLambda()表达式产生的函数类FunctionUtil.Func<?, ?>");
        }

        if (isEmpty(func)) {
            if (serializedLambdaThreadLocal.get() == null) {
                serializedLambdaThreadLocal.set(new HashMap<>());
            }
            SerializedLambda s = ClassUtils.serializeClass(ClassUtils.serializeByteArray(func), SerializedLambda.class);
            if (s != null) {
                serializedLambdaThreadLocal.get().put(func.getClass().getName(), s);
            }
        }
        return getSerializedLambda(func);
    }



    /**
     * 获取接口 class 名称
     *
     * @return 返回 class 名称
     */
    public static <T extends Function & Serializable> String functionalInterfaceClassName(T func) {
        return replaceSlash(getSerializedLambda(func).getFunctionalInterfaceClass());
    }


    /**
     * 获取实现的 class
     *
     * @return 实现类
     */
    public static <T extends Function & Serializable> Class<?> implClass(T func) {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // 无法访问线程上的类加载器ClassLoader
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // 线程类加载器获取失败使用当前类加载器.
            cl = FunctionUtil.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        try {
            return Class.forName(replaceSlash(getSerializedLambda(func).getImplClass()), false, cl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 如果传入的是get 或 set方法则返回此方法的字段名称
     *
     * @param func
     * @return
     */
    public static <T extends Function & Serializable> String fieldName(T func) {
        String methodName = implMethodName(func);
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            methodName = methodName.substring(3);
            methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
        }
        return methodName;
    }

    /**
     * 获取实现者的方法名称
     *
     * @return 方法名称
     */
    public static <T extends Function & Serializable> String implMethodName(T func) {
        return getSerializedLambda(func).getImplMethodName();
    }


    /**
     * @return 获取实例化方法的类型
     */
    public static <T extends Function & Serializable> Class instantiatedType(T func) {
        String instantiatedTypeName = replaceSlash(getSerializedLambda(func).getInstantiatedMethodType().substring(2, getSerializedLambda(func).getInstantiatedMethodType().indexOf(';')));
        return ClassUtils.toClassConfident(instantiatedTypeName);
    }

    /**
     * @return 字符串形式函数形参
     */
    public static <T extends Function & Serializable> String parameterToString(T func) {
        String implName = Objects.requireNonNull(implClass(func)).getName();
        return String.format("%s::%s",
                implName.substring(implName.lastIndexOf('.') + 1),
                getSerializedLambda(func).getImplMethodName());
    }

    /**
     * 将字符串中的 / 替换为 .
     *
     * @param str 需要替换的字符串
     * @return 替换后的字符串
     */
    private static String replaceSlash(String str) {
        return str.replace('/', '.');
    }

    /**
     * 线程下是否不存在 SerializedLambda 对象
     *
     * @return
     */
    private static <T extends Function & Serializable> boolean isEmpty(T func) {
        if (serializedLambdaThreadLocal.get() != null && serializedLambdaThreadLocal.get().containsKey(func.getClass().getName())) {
            return false;
        }
        return true;
    }

    /**
     * 获取线程中的 SerializedLambda 对象
     */
    private static <T extends Function & Serializable> SerializedLambda getSerializedLambda(T func) {
        if (isEmpty(func)) {
            resolveSLambda(func);
        }
        return serializedLambdaThreadLocal.get().get(func.getClass().getName());
    }
}
