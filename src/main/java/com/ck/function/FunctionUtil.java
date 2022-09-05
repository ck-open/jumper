
package com.ck.function;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Function 工具类
 *
 * @author cyk
 * @since 2021-10-01
 */
public final class FunctionUtil implements Serializable {
    static class TestF {
        String str;

        public TestF(String s) {
            this.str = s;
        }

        public String get() {
            return str;
        }
    }


    /**
     * 根据当前线程缓存SerializedLambda对象
     */
    private static ThreadLocal<Map<String, SerializedLambda>> serializedLambdaThreadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        TestF t = new TestF("测试成功1");
        test(TestF::get, t);
        test(TestF::get, t);
        test(TestF::get, t);


//        for (int i=0;i<10;i++){
//            new Thread(()->get(Prpcitemkind::getPolicyno, new Prpcitemkind().setPolicyno("测试成功"+Thread.currentThread().getName()))).start();
//        }


    }


    public static <T, R> void test(Func<? super T, ? extends R> keyExtractor, T o) {
        System.out.println(keyExtractor.apply(o) + get(keyExtractor));

//        System.out.println(getFunctionalInterfaceClassName());
//        System.out.println(getImplClass());
//        System.out.println(get());
//        System.out.println(getImplMethodName());
//        System.out.println(getInstantiatedType());

//        System.out.println(serializedLambda.getImplClass());
//        System.out.println(serializedLambda.getImplMethodName());
//        System.out.println(serializedLambda.getCapturedArgs());
//        System.out.println(serializedLambda.getCapturingClass());
//        System.out.println(serializedLambda.getFunctionalInterfaceClass());
//        System.out.println(serializedLambda.getFunctionalInterfaceMethodName());
//        System.out.println(serializedLambda.getFunctionalInterfaceMethodSignature());
//        System.out.println(serializedLambda.getImplMethodKind());
//        System.out.println(serializedLambda.getImplMethodSignature());
//        System.out.println(serializedLambda.getInstantiatedMethodType());
//        System.out.println(keyExtractor.apply(o));

    }

    /**
     * 通过反序列化转换 Function 函数表达式为SerializedLambda 对象并缓存到当前线程中
     *
     * @param func Function 函数对象
     * @return 返回解析后的 SerializedLambda
     */
    public static SerializedLambda resolveSLambda(FunctionUtil.Func<?, ?> func) {
        if (!func.getClass().isSynthetic()) {
            throw new RuntimeException("该方法仅能传入  Lambda::getLambda()表达式产生的函数类FunctionUtil.Func<?, ?>");
        }

        if (isEmpty(func)) {
            if (serializedLambdaThreadLocal.get() == null) {
                serializedLambdaThreadLocal.set(new HashMap<>());
            }
            SerializedLambda s = serializeClass(serializeByteArray(func), SerializedLambda.class);
            if (s != null) {
                serializedLambdaThreadLocal.get().put(func.getClass().getName(), s);
            }
        }
        return getSerializedLambda(func);
    }

    /**
     * 将给定字节数组序列化为对象。
     *
     * @param object 要序列化的字节数组
     * @return 表示序列化的对象
     */
    public static <T> T serializeClass(byte[] object, Class<T> cla) {
        if (object == null) {
            return null;
        }
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(object)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz = super.resolveClass(objectStreamClass);
                if (clazz == java.lang.invoke.SerializedLambda.class) {
                    clazz = cla;
                }
                return clazz;
            }
        }) {
            return cla.cast(objIn.readObject());
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("二进制序列化成对象失败 ", e);
        }
    }

    /**
     * 将给定对象序列化为字节数组。
     *
     * @param object 要序列化的对象
     * @return 表示对象的字节数组
     */
    public static byte[] serializeByteArray(Object object) {
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
    private static boolean isEmpty(FunctionUtil.Func<?, ?> func) {
        if (serializedLambdaThreadLocal.get() != null && serializedLambdaThreadLocal.get().containsKey(func.getClass().getName())) {
            return false;
        }
        return true;
    }

    /**
     * 获取线程中的 SerializedLambda 对象
     */
    private static SerializedLambda getSerializedLambda(FunctionUtil.Func<?, ?> func) {
        if (isEmpty(func)) {
            resolveSLambda(func);
        }
        return serializedLambdaThreadLocal.get().get(func.getClass().getName());
    }

    /**
     * 获取接口 class 名称
     *
     * @return 返回 class 名称
     */
    public static String getFunctionalInterfaceClassName(FunctionUtil.Func<?, ?> func) {
        return replaceSlash(getSerializedLambda(func).getFunctionalInterfaceClass());
    }


    /**
     * 获取实现的 class
     *
     * @return 实现类
     */
    public static Class<?> getImplClass(FunctionUtil.Func<?, ?> func) {
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
    public static String getFieldName(FunctionUtil.Func<?, ?> func) {
        String methodName = getImplMethodName(func);
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
    public static String getImplMethodName(FunctionUtil.Func<?, ?> func) {
        return getSerializedLambda(func).getImplMethodName();
    }


    /**
     * @return 获取实例化方法的类型
     */
    public static Class getInstantiatedType(FunctionUtil.Func<?, ?> func) {
        String instantiatedTypeName = replaceSlash(getSerializedLambda(func).getInstantiatedMethodType().substring(2, getSerializedLambda(func).getInstantiatedMethodType().indexOf(';')));
        return ClassUtils.toClassConfident(instantiatedTypeName);
    }

    /**
     * @return 字符串形式函数形参
     */
    public static String get(FunctionUtil.Func<?, ?> func) {
        String implName = getImplClass(func).getName();
        return String.format("%s::%s",
                implName.substring(implName.lastIndexOf('.') + 1),
                getSerializedLambda(func).getImplMethodName());
    }


    /**
     * 支持序列化的 Function
     *
     * @author cyk
     * @since 2021-10-01
     */
    @FunctionalInterface
    public interface Func<T, R> extends Function<T, R>, Serializable {
    }
}
