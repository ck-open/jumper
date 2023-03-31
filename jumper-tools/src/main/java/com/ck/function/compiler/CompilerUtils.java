package com.ck.function.compiler;

import com.ck.utils.FileUtil;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * @ClassName CompilerUtils
 * @Description TODO
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/31 9:51
 **/
public final class CompilerUtils {
    private static Logger log = Logger.getLogger(CompilerUtils.class.getName());

    private static final Charset charset = StandardCharsets.UTF_8;

    private CompilerUtils() {
    }

    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.even.test;");
        sb.append("import java.util.Map;\nimport java.text.DecimalFormat;\n");
        sb.append("public class Sum{\n");
        sb.append("private final DecimalFormat df = new DecimalFormat(\"#.#####\");\n");
        sb.append("public Double calculate(Map data){\n");
        sb.append("double d = (30*data.get(\"f1\") + 20*data.get(\"f2\") + 50*data.get(\"f3\"))/100;\n");
        sb.append("return Double.valueOf(df.format(d));}}");


        String path = "E:\\";

        FileUtil.writerTextFile(path+"Sum.java",sb.toString());

//        compiler(path,FileUtil.getFilePath(path+"\\Sum").getPath());

    }


//    public static void testInvoke(String className, String source)
//            throws ClassNotFoundException, IllegalAccessException,
//            InstantiationException, NoSuchMethodException,
//            InvocationTargetException {
//
//        final String SUFFIX = ".java";// 类名后面要跟的后缀
//
//        // 对source进行编译生成class文件存放在Map中，这里用bytecode接收
//        Map<String, byte[]> bytecode = DynamicLoader.compile(className + SUFFIX,
//                source);
//
//        // 加载class文件到虚拟机中，然后通过反射执行
//        @SuppressWarnings("resource")
//        DynamicLoader.MemoryClassLoader classLoader = new DynamicLoader.MemoryClassLoader(
//                bytecode);
//        Class<?> clazz = classLoader.loadClass("TestClass");
//        Object object = clazz.newInstance();
//
//        // 得到sayHello方法
//        Method sayHelloMethod = clazz.getMethod("sayHello", String.class);
//        sayHelloMethod.invoke(object, "This is the method called by reflect");
//
//        // 得到add方法
//        Method addMethod = clazz.getMethod("add", int.class, int.class);
//        Object returnValue = addMethod.invoke(object, 1024, 1024);
//        System.out.println(Thread.currentThread().getName() + ": "
//                + "1024 + 1024 = " + returnValue);
//
//        // 因为在main方法中，调用了add和sayHello方法，所以直接调用main方法就可以执行两个方法
//        Method mainMethod = clazz.getDeclaredMethod("main", String[].class);
//        mainMethod.invoke(null, (Object) new String[] {});
//    }


    /**
     * 通过类名和其代码（Java代码字符串），编译得到字节码，返回类名及其对应类的字节码，封装于Map中，值得注意的是，
     * 平常类中就编译出来的字节码只有一个类，但是考虑到内部类的情况， 会出现很多个类名及其字节码，所以用Map封装方便。
     *
     * @param javaName 类名
     * @param javaSrc  Java源码
     * @return map
     */
    public static Map<String, byte[]> compile(String javaName, String javaSrc) {
        // 调用java编译器接口
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler
                .getStandardFileManager(null, null, null);

        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(
                stdManager)) {

            @SuppressWarnings("static-access")
            JavaFileObject javaFileObject = manager.makeStringSource(javaName,
                    javaSrc);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager,
                    null, null, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                return manager.getClassBytes();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 先根据类名在内存中查找是否已存在该类，若不存在则调用 URLClassLoader的 defineClass方法加载该类
     * URLClassLoader的具体作用就是将class文件加载到jvm虚拟机中去
     *
     * @author Administrator
     *
     */
    public static class MemoryClassLoader extends URLClassLoader {
        Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

        public MemoryClassLoader(Map<String, byte[]> classBytes) {
            super(new URL[0], MemoryClassLoader.class.getClassLoader());
            this.classBytes.putAll(classBytes);
        }

        @Override
        protected Class<?> findClass(String name)
                throws ClassNotFoundException {
            byte[] buf = classBytes.get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            classBytes.remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }


}
