
package com.ck.function;

import com.ck.function.serializable.SerializedLambda;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
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
public final class LambdaUtils implements Serializable {

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
            Map<String, Class<?>> primClasses = new HashMap<>();
            primClasses.put("java.lang.invoke.SerializedLambda", SerializedLambda.class);
            SerializedLambda s = ClassUtils.deserialize(ClassUtils.serialize(func), SerializedLambda.class, primClasses);
            if (s != null) {
                serializedLambdaThreadLocal.get().put(func.getClass().getName(), s);
            }
        }
        return getSerializedLambda(func);
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
     * 获取接口 函数 class 名称
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
        return ClassUtils.getClassForName(replaceSlash(getSerializedLambda(func).getImplClass()));
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
    public static <T extends Function & Serializable> Class<?> instantiatedType(T func) {
        String instantiatedTypeName = replaceSlash(getSerializedLambda(func).getInstantiatedMethodType().substring(2, getSerializedLambda(func).getInstantiatedMethodType().indexOf(';')));
        return ClassUtils.getClassForName(instantiatedTypeName);
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
     * 获取指定字段get方法的 LambdaMetafac 对象
     *
     * @param fieldName   字段名称
     * @param entityClass 字段所属类
     * @param invokedType 返回值类型
     * @return
     * @throws Throwable
     */
    public static MethodHandle getFieldLambdaMethodHandle(String fieldName, Class<?> entityClass, Class<?> invokedType) {
        final int FLAG_SERIALIZABLE = 1;
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        if (fieldName != null && entityClass != null && invokedType != null) {
            try {
                String name = "get" + fieldName.substring(0, 1).toUpperCase();
                if (fieldName.length() > 1) name += fieldName.substring(1);

                Method method = entityClass.getDeclaredMethod(name);
                if (method != null) {
                    MethodType returnMethodType = MethodType.methodType(method.getReturnType(), entityClass);

                    //方法名叫做:getFieldName  转换为 invokedType function interface对象
                    final CallSite site = LambdaMetafactory.altMetafactory(lookup,
                            "invoke",
                            MethodType.methodType(invokedType),
                            returnMethodType,
                            lookup.findVirtual(entityClass, name, MethodType.methodType(method.getReturnType())),
                            returnMethodType, FLAG_SERIALIZABLE);

//            return (SFunction) site.getTarget().invokeExact();
                    return site.getTarget();
                }
            } catch (NoSuchMethodException | IllegalAccessException | LambdaConversionException e) {
                throw new IllegalArgumentException("获取get" + fieldName + "方法错误", e);
            }
        }
        return null;
    }
}
