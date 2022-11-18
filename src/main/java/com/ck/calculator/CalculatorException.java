package com.ck.calculator;

/**
 * @ClassName CalculatorException
 * @Description 计算异常类
 * @Author Cyk
 * @Version 1.0
 * @since 2022/11/18 15:11
 **/
public class CalculatorException extends RuntimeException {

    public CalculatorException() {
    }
    public CalculatorException(String message) {
        super(message);
    }
    public CalculatorException(String message, Throwable cause) {
        super(message, cause);
    }
    public CalculatorException(Throwable cause) {
        super(cause);
    }

    /**
     * 函数不存在
     */
    public static class FunctionInExistence extends CalculatorException{
        public FunctionInExistence() {
        }

        public FunctionInExistence(String message) {
            super(message);
        }

        public FunctionInExistence(String message, Throwable cause) {
            super(message, cause);
        }

        public FunctionInExistence(Throwable cause) {
            super(cause);
        }
    }

    /**
     * 函数不存在
     */
    public static class FunctionRun extends CalculatorException{
        public FunctionRun() {
        }

        public FunctionRun(String message) {
            super(message);
        }

        public FunctionRun(String message, Throwable cause) {
            super(message, cause);
        }

        public FunctionRun(Throwable cause) {
            super(cause);
        }
    }
}
