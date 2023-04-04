package com.ck.function;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName CompilerUtils
 * @Description 动态编译Java源码工具类
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/31 9:51
 **/
public final class JavaCompilerUtils {
    private static Logger log = Logger.getLogger(JavaCompilerUtils.class.getName());

    private static final Charset charset = StandardCharsets.UTF_8;

    private JavaCompilerUtils() {
    }

    public static void main(String[] args) throws ClassNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.ck.function.compiler;");
        sb.append("public class TestCompiler {");
        sb.append("private String name;");
        sb.append("private String address;");
        sb.append("public String getName() { return name; }");
        sb.append("public void setName(String name) {this.name = name;}");
        sb.append("public String getAddress() {return address;}");
        sb.append("public void setAddress(String address) {this.address = address;}");
        sb.append("}");

//        compiler(null, buildJavaFileObject(sb.toString()));
//
//        Class<?> c = ClassUtils.getClassLoader().loadClass("com.ck.function.compiler.TestCompiler");

        Map<String, Class<?>> classMap = compilerString(sb.toString());
        System.out.println(classMap);

        classMap.values().forEach(i -> {
            try {
                Class.forName(i.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 编译java源码并加载Class
     *
     * @param sourceCode
     * @return
     */
    public static boolean compilerFile(String... sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, sourceCode);
        log.info("java source file compilation " + (result == 0 ? "succeed" : "failed"));
        return result == 0;
    }

    /**
     * 编译java源码并加载Class
     *
     * @param sourceCode
     * @return
     */
    public static Map<String, Class<?>> compilerString(String... sourceCode) {
        // 构建文件管理器  StandardJavaFileManager实例
        JavaFileManager fileManager = new MemoryJavaFileManager(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null));

        // 编译源码 并 加载类
        return compiler(fileManager, buildJavaFileObject(sourceCode));
    }


    /**
     * 编译java源码
     *
     * @param javaFileObjects
     * @return
     */
    public static Map<String, Class<?>> compiler(JavaFileManager javaFileManager, List<? extends JavaFileObject> javaFileObjects) {
        // 获取编译器
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        // 构建默认文件管理器  StandardJavaFileManager实例
        if (javaFileManager == null) {
            javaFileManager = javaCompiler.getStandardFileManager(null, null, null);
        }

        boolean result = javaCompiler.getTask(null, javaFileManager, null, null, null, javaFileObjects).call();

        log.info("java source compilation " + (result ? "succeed" : "failed"));
        try {
            javaFileManager.close();
        } catch (IOException e) {
            log.warning("JavaFileManager close failed  message: " + e.getMessage());
        }

        // 加载类
        Map<String, Class<?>> classMap = new LinkedHashMap<>();
        JavaFileManager finalJavaFileManager = javaFileManager;
        javaFileObjects.forEach(javaFileObject -> {
            String className = removeSuffixes(javaFileObject.getName()).replaceAll("/", ".");
            ;
            try {
                if (result) {
                    classMap.put(className, finalJavaFileManager.getClassLoader(StandardLocation.CLASS_PATH).loadClass(javaFileObject.getName()));
                } else {
                    classMap.put(className, Class.forName(javaFileObject.getName()));
                }
            } catch (Exception e) {
                log.warning("java source ClassLoader failed className: " + javaFileObject.getName() + " message: " + e.getMessage());
            }
        });

        return classMap;
    }

    /**
     * 字符串源码构建JavaFileObject
     *
     * @param sourceCode
     * @return
     */
    public static List<JavaFileObject> buildJavaFileObject(String... sourceCode) {
        return Stream.of(sourceCode).map(code -> {
            String className = getSourceCodeClassName(code);
            return new CharJavaFileObject(className, code);
        }).collect(Collectors.toList());
    }

    /**
     * 字符串源码构建JavaFileObject
     *
     * @param sourceCode
     * @return
     */
    public static String getSourceCodeClassName(String sourceCode) {
        Objects.requireNonNull(sourceCode, "java source is null");

        if (!sourceCode.startsWith("package ")) {
            // 源码不是以 package 开头
            throw new UnknownFormatFlagsException("source code not package Starts With");
        }
        if (!sourceCode.contains("class")) {
            // 源码不包含class关键字
            throw new UnknownFormatFlagsException("source code not contains class flag");
        }
        if (!sourceCode.contains("{")) {
            // 源码格式错误
            throw new UnknownFormatFlagsException("source code not contains { flag");
        }

        String fullName = sourceCode.substring(0, sourceCode.indexOf(";")).replace("package", "").trim();

        String className = sourceCode.substring(sourceCode.indexOf("class"), sourceCode.indexOf("{")).replace("class", "");
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (s.length() > 0 && (c == ' ' || c == '<')) {
                break;
            }
            if (c >= 'a' || c <= 'Z') {
                s.append(c);
            }
        }
        fullName += "." + s.toString().trim() + JavaFileObject.Kind.SOURCE.extension;
        return fullName;
    }


    /**
     * 转换类名 目录结构为包结构 并去掉 .java 或 .class 后缀
     *
     * @param className
     * @return
     */
    public static String removeSuffixes(String className) {
        if (className != null) {
            if (className.startsWith("/")) {
                className = className.substring(1);
            }
            if (className.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
                className = className.replace(JavaFileObject.Kind.SOURCE.extension, "");
            } else if (className.endsWith(JavaFileObject.Kind.CLASS.extension)) {
                className = className.replace(JavaFileObject.Kind.CLASS.extension, "");
            }
        }
        return className;
    }

    /**
     * 将包路径转换为文件路径
     *
     * @param name
     * @return
     */
    public static URI toFileURI(String name) {
        String prefix = "memory:///";
        File file = new File(name);
        if (file.exists()) {// 如果文件存在，返回他的URI
            return file.toURI();
        } else {

            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append(prefix);
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
                    newUri.replace(newUri.length() - JavaFileObject.Kind.SOURCE.extension.length(), newUri.length(), JavaFileObject.Kind.SOURCE.extension);
                }
                return URI.create(newUri.toString());
            } catch (Exception exp) {
                return URI.create(prefix + "com/sun/script/java/java_source");
            }
        }
    }


    /**
     * 基于内存的JavaFile管理器
     */
    public static class MemoryJavaFileManager extends ForwardingJavaFileManager {
        /**
         * 存储编译后的代码数据
         * key:className
         */
        private Map<String, JavaClassObject> classJavaFileObject = new LinkedHashMap<>();

        public MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }


        /**
         * 编译后加载类
         * <p>
         * 返回一个匿名的SecureClassLoader:
         * 加载由JavaCompiler编译后，保存在ClassJavaFileObject中的byte数组。
         */
        @Override
        public ClassLoader getClassLoader(Location location) {
            return new SecureClassLoader() {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    String classFileName = JavaCompilerUtils.class.getResource("/").getPath()
                            + removeSuffixes(name) + JavaFileObject.Kind.CLASS.extension;

                    name = removeSuffixes(name).replaceAll("/", ".");
                    if (!classJavaFileObject.containsKey(name)) {
                        throw new NullPointerException("MemoryJavaFileManager JavaClassObject nonentity ClassName: " + name);
                    }

                    // 检查包路径 不存在则创建
                    String path = classFileName.substring(0, classFileName.lastIndexOf("/"));
                    File classPathFile = new File(path);
                    if (!classPathFile.exists()) {
                        classPathFile.mkdirs();
                    }

                    // 写出 .class 文件
                    File classFile = new File(classFileName);
                    if (!classFile.exists()) {
                        try (FileOutputStream writer = new FileOutputStream(classFile)) {
                            writer.write(classJavaFileObject.get(name).getBytes());
                        } catch (IOException e) {
                            log.warning("class file write failed className: " + name);
                        }
                    }

                    byte[] bytes = classJavaFileObject.get(name).getBytes();
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
            };
        }

        /**
         * 给编译器提供JavaClassObject，编译器会将编译结果写进去
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaClassObject classObject = new JavaClassObject(className, kind);
            this.classJavaFileObject.put(className, classObject);
            return classObject;
        }
    }


    /**
     * 存储源代码
     * 字符串java源代码。JavaFileObject表示
     */
    public static class CharJavaFileObject extends SimpleJavaFileObject {

        /**
         * 类名
         */
        private String className;
        /**
         * 表示java源代码
         */
        private CharSequence content;

        public CharJavaFileObject(String className, String content) {
//            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            super(toFileURI(className), Kind.SOURCE);
            this.className = className;
            this.content = content;
        }

        /**
         * 返回className
         *
         * @return
         */
        public String getClassName() {
            return this.className;
        }

        /**
         * 获取需要编译的源代码
         *
         * @param ignoreEncodingErrors
         * @return
         * @throws IOException
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }
    }

    /**
     * 存储编译后的字节码
     */
    public static class JavaClassObject extends SimpleJavaFileObject {

        /**
         * Compiler编译后的byte数据会存在这个ByteArrayOutputStream对象中，
         * 后面可以取出，加载到JVM中。
         */
        private ByteArrayOutputStream byteArrayOutputStream;

        public JavaClassObject(String className, Kind kind) {
//            super(URI.create("string:///" + className.replaceAll("\\.", "/") + kind.extension), kind);
            super(toFileURI(className), kind);
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }

        /**
         * 覆盖父类SimpleJavaFileObject的方法。
         * 该方法提供给编译器结果输出的OutputStream。
         * <p>
         * 编译器完成编译后，会将编译结果输出到该 OutputStream 中，我们随后需要使用它获取编译结果
         *
         * @return
         * @throws IOException
         */
        @Override
        public OutputStream openOutputStream() throws IOException {
            return this.byteArrayOutputStream;
        }

        /**
         * FileManager会使用该方法获取编译后的byte，然后将类加载到JVM
         */
        public byte[] getBytes() {
            return this.byteArrayOutputStream.toByteArray();
        }
    }

}
