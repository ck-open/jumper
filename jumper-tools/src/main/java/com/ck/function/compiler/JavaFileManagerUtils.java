package com.ck.function.compiler;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName JavaFileObjectUtils
 * @Description JavaFileObject
 * @Author Cyk
 * @Version 1.0
 * @since 2023/4/3 9:47
 **/
public class JavaFileManagerUtils {
    public final static String EXT = JavaFileObject.Kind.SOURCE.extension;// Java源文件的扩展名

    public static URI toURI(String name) {
        File file = new File(name);
        if (file.exists()) {// 如果文件存在，返回他的URI
            return file.toURI();
        } else {

            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(EXT)) {
                    newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
                }
                return URI.create(newUri.toString());
            } catch (Exception exp) {
                return URI.create("mfm:///com/sun/script/java/java_source");
            }
        }
    }

    /**
     * 构建字符串表述的源码 JavaFileObject
     *
     * @param name
     * @param code
     * @return
     */
    public static JavaFileObject makeStringSourceJavaFileObject(String name, String code) {
        return new JavaFileManagerUtils.StringSimpleJavaFileObject(name, code);
    }


    /**
     * 将编译好的.class文件保存到内存当中，这里的内存也就是map映射当中
     */
    @SuppressWarnings("rawtypes")
    public static final class MemoryJavaFileManager extends ForwardingJavaFileManager {


        private Map<String, byte[]> classBytes;// 用于存放.class文件的内存

        @SuppressWarnings("unchecked")
        public MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
            classBytes = new HashMap<String, byte[]>();
        }

        public Map<String, byte[]> getClassBytes() {
            return classBytes;
        }

        @Override
        public void close() throws IOException {
            classBytes = new HashMap<String, byte[]>();
        }

        @Override
        public void flush() throws IOException {
        }


        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new JavaFileManagerUtils.ClassSimpleJavaFileObject(className, classBytes);
            } else {
                return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        }
    }


    /**
     * 一个文件对象，用来表示从string中获取到的source，一下类容是按照jkd给出的例子写的
     */
    public static class StringSimpleJavaFileObject extends SimpleJavaFileObject {
        // The source code of this "file".
        final String code;

        /**
         * Constructs a new JavaSourceFromString.
         *
         * @param name 此文件对象表示的编译单元的name
         * @param code 此文件对象表示的编译单元source的code
         */
        StringSimpleJavaFileObject(String name, String code) {
            super(toURI(name), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(code);
        }

        @SuppressWarnings("unused")
        public Reader openReader() {
            return new StringReader(code);
        }
    }

    /**
     * 将Java字节码存储到classBytes映射中的文件对象
     */
    public static class ClassSimpleJavaFileObject extends SimpleJavaFileObject {
        private String name;

        private Map<String, byte[]> classFileObject;

        /**
         * @param name className
         */
        ClassSimpleJavaFileObject(String name, Map<String, byte[]> classBytes) {
            super(toURI(name), Kind.CLASS);
            this.name = name;
            this.classFileObject = classBytes;
        }

        @Override
        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    classFileObject.put(name, bos.toByteArray());
                }
            };
        }
    }
}
