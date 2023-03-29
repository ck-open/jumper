package com.ck.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * 异常对象操作工具
 *
 * @author cyk
 * @since 2021-09-01
 */
public class ExceptionUtil {
    private static Logger logger = Logger.getLogger(ExceptionUtil.class.getName());

    /**
     * 获取异常对象的错误堆栈日志文本
     *
     * @param e             异常对象
     * @param packagePrefix 指定要打印的堆栈日志中包的前缀
     * @return 仿：Exception.printStackTrace(); 的输出日志
     */
    public static String getExceptionStackTraceInfo(Throwable e, String... packagePrefix) {
        return getExceptionStackTraceInfo(e, null, packagePrefix);
    }

    /**
     * 获取异常对象的错误堆栈日志文本
     *
     * @param e             异常对象
     * @param rowNum        要打印的最终堆栈信息行数
     * @param packagePrefix 指定要打印的堆栈日志中包的前缀
     * @return 仿：Exception.printStackTrace(); 的输出日志
     */
    public static String getExceptionStackTraceInfo(Throwable e, Integer rowNum, String... packagePrefix) {
        if (e == null) {
            return null;
        }
        String enter = "\r\n";
        StringBuffer result = new StringBuffer(e.getClass().getName() + ": " + e.getLocalizedMessage() + enter);

        if (rowNum == null || rowNum < 1) rowNum = e.getStackTrace().length;
        for (StackTraceElement ste : e.getStackTrace()) {
            if (rowNum <= 0) break;
            boolean off = true;
            if (packagePrefix != null && packagePrefix.length > 0) {
                off = false;
                for (String prefix : packagePrefix) {
                    if (ste.getClassName().startsWith(prefix)) {
                        off = true;
                    }
                }
            }
            if (off) {
                result.append("\tat ").append(ste.getClassName()).append(".").append(ste.getMethodName()).append("(").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append(")").append(enter);
                rowNum--;
            }
        }

        if (e.getCause()!=null){
            result.append("\r\r").append(getExceptionStackTraceInfo(e.getCause(), rowNum,packagePrefix));
        }
        return result.toString();
    }


    /**
     * 将异常对象的错误堆栈日志文本导出到本地文件
     *
     * @param e         异常对象
     * @param errorData 发生异常的数据文本
     * @param fileName  文件名
     * @param errorPath 文件地址
     */
    public static String exceptionStackTraceExportToFile(Exception e, String errorData, String fileName, String errorPath) {
        try {
            if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                // Windows系统时 默认为开发测试启动，将错误日期文件写出到本地
                errorPath = new File("").getCanonicalPath() + File.separator + "temp" + File.separator;
            }

            if (!errorPath.endsWith(File.separator)) {
                errorPath += File.separator;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator);
            errorPath += simpleDateFormat.format(new Date());

            File fileDir = new File(errorPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            String errorContent = "";
            if (errorData != null && !"".equals(errorData.trim())) {
                errorContent += "发生异常的数据：\r";
                errorContent += errorData + "\r\r";
            }

            errorContent += "异常堆栈追踪：\r" + getExceptionStackTraceInfo(e);

            fileName += ".txt";
            File errorFile = new File(errorPath + fileName);
            if (!errorFile.getParentFile().exists()) {
                errorFile.getParentFile().mkdirs();
            }
            if (!errorFile.exists()) {
                errorFile.createNewFile();
            }

            FileWriter resultFile = new FileWriter(errorFile, false);
            PrintWriter print = new PrintWriter(resultFile);
            print.println(errorContent);
            resultFile.close();
        } catch (Exception ex) {
            logger.warning("捕获的异常写出到本地文档失败，发生异常：" + getExceptionStackTraceInfo(ex));
            logger.warning("捕获到未写出的异常：" + getExceptionStackTraceInfo(e));
        }
        return errorPath + fileName;
    }


    public static void main(String[] args) {
        try {
            try {
                try {

                    "".substring(0, 10);
                }catch (Exception e){
                    throw new Exception("自定义1",e);
                }
            }catch (Exception e){
                throw new RuntimeException("自定义2",e);
            }
        } catch (Exception e) {
           String msg =  getExceptionStackTraceInfo(e);
            System.out.println(msg);


        }
    }
}
