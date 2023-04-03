package com.ck.function.compiler;

import com.ck.utils.FileUtil;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureClassLoader;
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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
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


        String fullName = "com.ck.function.compiler.TestCompiler.java";
        String sourceCode = sb.toString();

        // 获取编译器
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        // 通过DiagnosticListener得到诊断信息，而DiagnosticCollector类就是listener的实现。
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector();

        // 构建文件管理器  StandardJavaFileManager实例
        JavaFileManager fileManager = new MemoryJavaFileManager(javaCompiler.getStandardFileManager(diagnosticCollector, null, null));

        List<JavaFileObject> javaFileObjectList = new ArrayList<>();
        javaFileObjectList.add(new CharJavaFileObject(fullName, sourceCode));

        // 编译源码
        boolean result = javaCompiler.getTask(null, fileManager, null, null, null, javaFileObjectList).call();

        // 加载类
        if (result) {
            fileManager.getClassLoader(null).loadClass(fullName);
        } else {
            Class.forName(fullName);
        }

    }


    /**
     * 将包路径转换为文件路径
     *
     * @param name
     * @return
     */
    public static URI toFileURI(String name) {
        File file = new File(name);
        if (file.exists()) {// 如果文件存在，返回他的URI
            return file.toURI();
        } else {

            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
                    newUri.replace(newUri.length() - JavaFileObject.Kind.SOURCE.extension.length(), newUri.length(), JavaFileObject.Kind.SOURCE.extension);
                }
                return URI.create(newUri.toString());
            } catch (Exception exp) {
                return URI.create("mfm:///com/sun/script/java/java_source");
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

        //表示java源代码
        private CharSequence content;

        protected CharJavaFileObject(String className, String content) {
//            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            super(toFileURI(className), Kind.SOURCE);
            this.content = content;
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
