package com.ck.utils;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class 操作工具类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class ClassUtil {
    private static final Logger log = Logger.getLogger(ClassUtil.class.getName());

    /**
     * 根据指定的类型创建实例对象
     *
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T newInstance(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.warning(String.format("T newInstance Error：%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 获取对象泛型类型列表
     * 泛型必须在继承关系中指明真实类型，才能在反射中回去到。
     *
     * @param o     泛型类子类的实例对象
     * @param index 要获取的泛型类型在泛型列表中的位置
     * @return
     */
    public static <T> Class<T> getGenericSuperclass(Object o, int index) {
        Type[] types = getGenericSuperclass(o);
        if (types != null && types.length >= index) {
            return (Class<T>) types[index];
        }
        return null;
    }

    /**
     * 获取对象泛型类型列表
     * 泛型必须在继承关系中指明真实类型，才能在反射中回去到。
     *
     * @param o
     * @return
     */
    public static Type[] getGenericSuperclass(Object o) {

        // o类的父类(Base<T>，以为User为例，那就是Base<User>)
        Type type = o.getClass().getGenericSuperclass(); // generic 泛型
        // 强制转化“参数化类型”
        ParameterizedType parameterizedType = (ParameterizedType) type;
        // 参数化类型中可能有多个泛型参数
        Type[] types = parameterizedType.getActualTypeArguments();
        // 获取数据的第一个元素(User.class)
        return types;
    }

    /**
     * @description 获取接口指定的泛型类型
     * @author Cyk
     * @since 14:05 2022/4/22
     * @param o 实现接口的实体对象
     * @param interfaces  指定要获取的接口，不指定则获取实体对象实现的所有接口泛型
     * @return Map<String,Type>  key: 接口className  v:对应接口的泛型列表
     **/
    public static Map<String, Type[]> getGenericInterfaces(Object o, Class<?>... interfaces) {
        Set<String> interfacesClassName = null;

        if (interfaces.length > 0) {
            interfacesClassName = new HashSet<>();
            for (Class<?> a : interfaces) {
                interfacesClassName.add(a.getName());
            }
        }

        Type[] interfacesTypes = o.getClass().getGenericInterfaces();

        Map<String, Type[]> result = new LinkedHashMap<>();
        for (Type t : interfacesTypes) {
            if (interfacesClassName != null) {
                String tName = t.getTypeName();
                if (tName.contains("<")) tName = tName.substring(0, tName.indexOf("<"));
                if (!interfacesClassName.contains(tName)) {
                    continue;
                }

                Type[] genericType = ((ParameterizedType) t).getActualTypeArguments();
                result.put(tName, genericType);
            }
        }
        return result;
    }

    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            log.warning(String.format("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e));
        }
        return null;
    }


    /**
     * 对象转字节数组
     */
    public static byte[] objectToBytes(Object obj) {
        if (obj != null) {
            try (
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ObjectOutputStream sOut = new ObjectOutputStream(out);
            ) {
                sOut.writeObject(obj);
                sOut.flush();
                byte[] bytes = out.toByteArray();
                return bytes;
            } catch (IOException e) {
                log.warning(String.format("objectToBytes error:%s", e.getMessage()));
            }
        }
        return null;
    }

    /**
     * 字节数组转对象
     */
    public static Object bytesToObject(byte[] bytes) {
        if (bytes != null) {
            try (
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                    ObjectInputStream sIn = new ObjectInputStream(in);
            ) {
                return sIn.readObject();
            } catch (IOException | ClassNotFoundException e) {
                log.warning(String.format("bytesToObject error:%s", e.getMessage()));
            }
        }
        return null;
    }

    /**
     * 获取指定字段get方法的MethodHandle对象
     * @param fieldName   字段名称
     * @param entityClass 字段所属类
     * @param invokedType 返回值类型
     * @return
     * @throws Throwable
     */
    public static MethodHandle getFieldMethodHandle(String fieldName, Class<?> entityClass, Class<?> invokedType) throws Throwable {
        final int FLAG_SERIALIZABLE = 1;
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        String name = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = entityClass.getDeclaredMethod(name);
        if (method != null) {
            MethodType methodType = MethodType.methodType(method.getReturnType(), entityClass);

            //方法名叫做:getSecretLevel  转换为 SFunction function interface对象
            final CallSite site = LambdaMetafactory.altMetafactory(lookup,
                    "invoke",
//                    MethodType.methodType(SFunction.class),
                    MethodType.methodType(invokedType),
                    methodType,
                    lookup.findVirtual(entityClass, name, MethodType.methodType(method.getReturnType())),
                    methodType, FLAG_SERIALIZABLE);

//            return (SFunction) site.getTarget().invokeExact();
            return site.getTarget();
        }
        return null;
    }

}
