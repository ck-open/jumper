package com.ck.function.compiler;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TestCompiler
 * @Description 测试动态编译
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/23 16:29
 **/
public class TestCompiler {


    /**
     * 存储源代码
     * 字符串java源代码。JavaFileObject表示
     */
    public static class CharSequenceJavaFileObject extends SimpleJavaFileObject {

        //表示java源代码
        private CharSequence content;

        protected CharSequenceJavaFileObject(String className, String content) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        /**
         * 获取需要编译的源代码
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
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + kind.extension), kind);
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }

        /**
         * 覆盖父类SimpleJavaFileObject的方法。
         * 该方法提供给编译器结果输出的OutputStream。
         *
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



    /**
     * 输出字节码到JavaClassFile
     */
    public static class ClassFileManager extends ForwardingJavaFileManager {

        /**
         * 存储编译后的代码数据
         */
        private JavaClassObject classJavaFileObject;

        protected ClassFileManager(JavaFileManager fileManager) {
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
                    byte[] bytes = classJavaFileObject.getBytes();
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
            };
        }

        /**
         * 给编译器提供JavaClassObject，编译器会将编译结果写进去
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            this.classJavaFileObject = new JavaClassObject(className, kind);
            return this.classJavaFileObject;
        }
    }


    /**
     * 运行时编译
     */
    public static class DynamicCompiler {
        private JavaFileManager fileManager;

        public DynamicCompiler() {
            this.fileManager = initManger();
        }

        private JavaFileManager initManger() {
            if (fileManager != null) {
                return fileManager;
            } else {
                JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
                DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
                fileManager = new ClassFileManager(javaCompiler.getStandardFileManager(diagnosticCollector, null, null));
                return fileManager;
            }
        }

        /**
         * 编译源码并加载，获取Class对象
         * @param fullName
         * @param sourceCode
         * @return
         * @throws ClassNotFoundException
         */
        public Class compileAndLoad(String fullName, String sourceCode) throws ClassNotFoundException {
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
            List<JavaFileObject> javaFileObjectList = new ArrayList<JavaFileObject>();
            javaFileObjectList.add(new CharSequenceJavaFileObject(fullName, sourceCode));
            boolean result = javaCompiler.getTask(null, fileManager, null, null, null, javaFileObjectList).call();
            if (result) {
                return this.fileManager.getClassLoader(null).loadClass(fullName);
            } else {
                return Class.forName(fullName);
            }
        }

        /**
         * 关闭fileManager
         * @throws IOException
         */
        public void closeFileManager() throws IOException {
            this.fileManager.close();
        }

    }


    public static void main(String[] args) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
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

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        Class cla = dynamicCompiler.compileAndLoad("com.ck.function.compiler.TestCompiler",sb.toString());
        dynamicCompiler.closeFileManager();

        Object obj = cla.newInstance();
        System.out.println(cla.getName());




        String fullName = "com.ck.function.compiler.TestCompiler";
        String sourceCode = sb.toString();

        // 获取编译器
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        // 通过DiagnosticListener得到诊断信息，而DiagnosticCollector类就是listener的实现。
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector();

        // 构建文件管理器  StandardJavaFileManager实例
        JavaFileManager fileManager = new ClassFileManager(javaCompiler.getStandardFileManager(diagnosticCollector, null, null));

        List<JavaFileObject> javaFileObjectList = new ArrayList<>();
        javaFileObjectList.add(new CharSequenceJavaFileObject(fullName, sourceCode));

        // 编译源码
        boolean result = javaCompiler.getTask(null, fileManager, null, null, null, javaFileObjectList).call();

        // 加载类
        if (result) {
            fileManager.getClassLoader(null).loadClass(fullName);
        } else {
            Class.forName(fullName);
        }
    }



}
