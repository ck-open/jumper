package com.ck.function;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class 操作工具类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class ClassUtils {
    private static final Logger log = Logger.getLogger(ClassUtils.class.getName());

    private ClassUtils() {
    }

    private static final char PACKAGE_SEPARATOR = '.';
    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList("net.sf.cglib.proxy.Factory"
            // cglib
            , "org.springframework.cglib.proxy.Factory"
            , "javassist.util.proxy.ProxyObject"
            // javassist
            , "org.apache.ibatis.javassist.util.proxy.ProxyObject");


    /**
     * 判断传入的类型是否是布尔类型
     *
     * @param type 类型
     * @return 如果是原生布尔或者包装类型布尔，均返回 true
     */
    public static boolean isBoolean(Class<?> type) {
        return type == boolean.class || Boolean.class == type;
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * 获取当前对象的 class
     * </p>
     *
     * @param clazz 传入
     * @return 如果是代理的class，返回父 class，否则返回自身
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }


    /**
     * <p>
     * 根据指定的 class ， 实例化一个对象，根据构造参数来实例化
     * </p>
     * <p>
     * 在 java9 及其之后的版本 Class.newInstance() 方法已被废弃
     * </p>
     *
     * @param clazz 需要实例化的对象
     * @param <T>   类型，由输入类型决定
     * @return 返回新的实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("实例化对象时出现错误,请尝试给 %s 添加无参的构造方法", clazz.getName()), e);
        }
    }

    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> toClassConfident(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e);
        }
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
        return parameterizedType.getActualTypeArguments();
    }

    /**
     * @param o          实现接口的实体对象
     * @param interfaces 指定要获取的接口，不指定则获取实体对象实现的所有接口泛型
     * @return Map<String, Type>  key: 接口className  v:对应接口的泛型列表
     * @description 获取接口指定的泛型类型
     * @author Cyk
     * @since 14:05 2022/4/22
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
            throw new IllegalArgumentException("找不到指定的class！请仅在确定会有 class 的时候，调用该方法", e);
        }
    }


    /**
     * 将给定字节数组序列化为对象。
     *
     * @param object 要序列化的字节数组
     * @return 表示序列化的对象
     */
    public static <T extends Serializable> T serializeClass(byte[] object, Class<T> cla) {
        if (object == null) {
            return null;
        }
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(object)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz = super.resolveClass(objectStreamClass);
                if (clazz == SerializedLambda.class) {
                    clazz = cla;
                }
                return clazz;
            }
        }) {
            return cla.cast(objIn.readObject());
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("二进制序列化成对象失败 ", e);
        }
    }

    /**
     * 将给定对象序列化为字节数组。
     *
     * @param object 要序列化的对象
     * @return 表示对象的字节数组
     */
    public static <T extends Serializable> byte[] serializeByteArray(T object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new IllegalArgumentException("对象序列化成byte[]失败: " + object.getClass(), e);
        }
        return byteArrayOutputStream.toByteArray();
    }


    /**
     * 获取指定字段get方法的MethodHandle对象
     *
     * @param fieldName   字段名称
     * @param entityClass 字段所属类
     * @param invokedType 返回值类型
     * @return
     * @throws Throwable
     */
    public static MethodHandle getFieldMethodHandle(String fieldName, Class<?> entityClass, Class<?> invokedType) {
        final int FLAG_SERIALIZABLE = 1;
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        if (fieldName != null && entityClass != null && invokedType != null) {
            try {
                String name = "get" + fieldName.substring(0, 1).toUpperCase();
                if (fieldName.length() > 1) name += fieldName.substring(1);

                Method method = entityClass.getDeclaredMethod(name);
                if (method != null) {
                    MethodType methodType = MethodType.methodType(method.getReturnType(), entityClass);

                    //方法名叫做:getFieldName  转换为 invokedType function interface对象
                    final CallSite site = LambdaMetafactory.altMetafactory(lookup,
                            "invoke",
                            MethodType.methodType(invokedType),
                            methodType,
                            lookup.findVirtual(entityClass, name, MethodType.methodType(method.getReturnType())),
                            methodType, FLAG_SERIALIZABLE);

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
