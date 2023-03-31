package com.ck.function.compiler;

import com.ck.utils.FileUtil;

import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        compiler(path,FileUtil.getFilePath(path+"\\Sum").getPath());

    }


    public static void compile(String sourceCodeFile, File outPath,String... ops) {
        Objects.requireNonNull(sourceCodeFile, "原代码为空");

        JavaCompiler javaCompiler = getJavaCompiler();
        InputStream first = null; // 程序的输入  null 用 system.in
        OutputStream second = null; // 程序的输出 null 用 system.out
        OutputStream third = null;  // 程序的错误输出 .,null 用 system.err
        // 程序编译参数 注意 我们编译目录是我们的项目目录
//        String[] strings = {"-d", ".", "test/Hello.java"};
       //  "-Xlint:unchecked","-classpath",System.getProperty("java.class.path")
//        // 0 表示成功， 其他表示出现了错误
        int result = javaCompiler.run(first, second, third,ops);
        log.info("Java原代码动态编译" + (result == 0 ? "成功" : "失败"));
    }

    /**
     * 获取 JavaCompiler 编译器
     *
     * @return
     */
    public static JavaCompiler getJavaCompiler() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

//        //获取java文件管理类
//        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
//
//        //获取java文件对象迭代器
//        Iterable<? extends JavaFileObject> it = manager.getJavaFileObjects("/javaFile.java");
//
//        //设置编译参数
//        ArrayList ops = new ArrayList();
//        ops.add("-Xlint:unchecked");
//        //设置classpath  当我们要编译的源代码中，引用了其他代码，我们需要将引用代码路径设置到-classpath中，否则会编译失败。
//        ops.add("-classpath");
//        //获取编译任务
//        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, it);
//        // 执行编译任务
//        task.call();

        Objects.requireNonNull(compiler, "获取 JavaCompiler 编译器失败");
        return compiler;
    }





    public static void compiler(String outPath,String... sources) throws IOException {
        //1.获得系统编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        //2. 建立DiagnosticCollector对象
        DiagnosticCollector<Object> diagnosticCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);

        //3. 建立源文件对象，每一个文件都被保存在一个JavaFileObject继承的类中
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(
                Arrays.asList(sources));

        //4. 确定options命令行选项
        List<String> options = Arrays.asList("-d", outPath);

        //5. 获取编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, options, null, compilationUnits);

        //6. 编译源程序
        Boolean success = task.call();
        fileManager.close();
        System.out.println(success ? "编译成功" : "编译失败");

        //7. 打印信息
        for (Diagnostic<?> diagnostic : diagnosticCollector.getDiagnostics()) {
            System.out.printf("Code: %s%n" + "Kind: %s%n" + "Position: %s%n" + "Start Position: %s%n"
                            + "End Position: %s%n" + "Source: %s%n" + "Message: %s%n", diagnostic.getCode(),
                    diagnostic.getKind(), diagnostic.getPosition(), diagnostic.getStartPosition(),
                    diagnostic.getEndPosition(), diagnostic.getSource(), diagnostic.getMessage(null));
        }
    }
}
