//package com.ck.function;
//
//import javax.tools.JavaCompiler;
//import javax.tools.JavaFileObject;
//import javax.tools.StandardJavaFileManager;
//import javax.tools.ToolProvider;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Logger;
//
//public class ClassUtil {
//    Logger logger = Logger.getLogger(ClassUtil.class.getName());
//
//    private static JavaCompiler compiler;
//
//    static {
//
//        compiler = ToolProvider.getSystemJavaCompiler();
//
//    }
//
//    /**
//     * 获取java文件路径
//     *
//     * @param file
//     * @return
//     */
//
//    private static String getFilePath(String file) {
//
//        int last1 = file.lastIndexOf('/');
//
//        int last2 = file.lastIndexOf('\\');
//
//        return file.substring(0, last1 > last2 ? last1 : last2) + File.separatorchar;
//
//    }
//
//    /**
//     * 编译java文件
//     *
//     * @param ops   编译参数
//     * @param files 编译文件
//     */
//
//    private static void javac(List ops, String... files) {
//
//        StandardJavaFileManager manager = null;
//
//        try {
//
//            manager = compiler.getStandardFileManager(null, null, null);
//
//            Iterable<? extends JavaFileObject> it = manager.getJavaFileObjects(files);
//
//            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, it);
//
//            task.call();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (manager != null) {
//                try {
//                    manager.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    /**
//     * 生成java文件
//     *
//     * @param file   文件名
//     * @param source java代码
//     * @throws Exception
//     */
//
//    private static void writeJavaFile(String file, String source) throws Exception {
//
//        BufferedWriter bw = null;
//        try {
//            File dir = new File(getFilePath(file));
//            if (!dir.exists())
//                dir.mkdirs();
//            bw = new BufferedWriter(new FileWriter(file));
//            bw.write(source);
//            bw.flush();
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            if (bw != null) {
//                bw.close();
//            }
//        }
//    }
//
//
///**
// * 编译代码并加载类
// * @param filePath java代码路径
//
// * @param source java代码
//
// * @param clsName 类名
//
// * @param ops 编译参数
//
// * @return
//
// */
//
//    public static Class loadClass(String filePath, String source, String clsName, List ops) {
//
//        try {
//
//            writeJavaFile(CLASS_PATH + filePath, source);
//
//            javac(ops, CLASS_PATH + filePath);
//
//            return load(clsName);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//
//    }
//
//    /**
//     * 调用类方法
//     *
//     * @param cls        类
//     * @param methodName 方法名
//     * @param paramsCls  方法参数类型
//     * @param params     方法参数
//     * @return
//     */
//
//    public static Object invoke(Class>cls, String methodName, Class>[]paramsCls, Object[] params) {
//
//        Object result = null;
//
//        try {
//
//            Method method = cls.getDeclaredMethod(methodName, paramsCls);
//
//            Object obj = cls.newInstance();
//
//            result = method.invoke(obj, params);
//
//        } catch (Exception e) {
//
//            logger.error(e);
//
//        }
//
//        return result;
//
//    }
//
//}
//
//public class ClassUtilTest {
//
//    private static final Log logger = LogFactory.getLog(ClassUtilTest.class);
//
//    public static void main(String args[]) {
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("package com.even.test;");
//
//        sb.append("import java.util.Map;\nimport java.text.DecimalFormat;\n");
//
//        sb.append("public class Sum{\n");
//
//        sb.append("private final DecimalFormat df = new DecimalFormat(\"#.#####\");\n");
//
//        sb.append("public Double calculate(Map data){\n");
//
//        sb.append("double d = (30*data.get(\"f1\") + 20*data.get(\"f2\") + 50*data.get(\"f3\"))/100;\n");
//
//        sb.append("return Double.valueOf(df.format(d));}}\n");
//
////设置编译参数
//
//        ArrayList ops = new ArrayList();
//
//        ops.add("-Xlint:unchecked");
//
////编译代码，返回class
//
//        Class > cls = ClassUtil.loadClass("/com/even/test/Sum.java", sb.toString(), "com.even.test.Sum", ops);
//
////准备测试数据
//
//        Map data = new HashMap();
//
//        data.put("f1", 10.0);
//
//        data.put("f2", 20.0);
//
//        data.put("f3", 30.0);
//
////执行测试方法
//
//        Object result = ClassUtil.invoke(cls, "calculate", new Class[]{Map.class}, new Object[]{data});
//
////输出结果
//
//        logger.debug(data);
//
//        logger.debug("(30*f1+20*f2+50*f3)/100 = " + result);
//
//    }
//}