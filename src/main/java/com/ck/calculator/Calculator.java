package com.ck.calculator;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Cyk
 * @description 函数表达式计算器
 * @since 18:09 2022/11/18
 **/
public class Calculator {
    private Logger log = Logger.getLogger(Calculator.class.getName());

    /**
     * 获取支持的计算符和函数名列表
     *
     * @param functionClass
     * @return
     */
    public static Set<String> getTag(Class<?> functionClass) {

        Set<String> tag = new LinkedHashSet<>(Arrays.asList("+", "-", "*", "/", "(", ")", ",", "<", ">", "<>", "><", "==", "!=","{","}"));
        // 默认函数
        for (Method m : Function.class.getDeclaredMethods()) {
            if (m.getName().contains("$") || m.getName().contains("isTrue")) continue;
            tag.add(m.getName().toLowerCase());
            tag.add(m.getName().toUpperCase());
        }

        // 自定义函数
        if (functionClass != null) {
            for (Method m : functionClass.getDeclaredMethods()) {
                tag.add(m.getName().toLowerCase());
                tag.add(m.getName().toUpperCase());
            }
        }
        return tag;
    }

    /**
     * 默认计算精度
     */
    private int scale = 10;

    /**
     * 表达式队列
     */
    private Queue<String> expressQueue = new ArrayDeque<>();

    /**
     * 自定义的函数类
     */
    private Class<?> functionClass = null;


    /**
     * 支持的函数名列表
     */
    private Map<String, String> functionNames = new HashMap<>();

    public Calculator() {
        this.setFunctionClass(Function.class);
    }

    /**
     * 设置默认保留精度
     *
     * @param scale
     */
    public Calculator setScale(int scale) {
        this.scale = scale;
        return this;
    }

    /**
     * 设置自定义函数类
     */
    public Calculator setFunctionClass(Class<?> functionClass) {
        if (functionClass != null) {
            this.functionClass = functionClass;

            // 记录所有以实现的函数名列表
            for (Method m : this.functionClass.getDeclaredMethods()) {
                this.functionNames.put(m.getName().toLowerCase(), m.getName());
            }
        }
        return this;
    }


    /**
     * 执行计算
     *
     * @param equation
     * @return
     */
    public BigDecimal calculate(String equation) {
        // 清除占位符标记
        resolution(equation.replaceAll("\\{","").replaceAll("}",""));
        BigDecimal result = calculate();
        log.info("Function Calculate Result: " + equation + " = " + result);
        return result;
    }

    private BigDecimal calculate() {
        if (expressQueue.isEmpty()) {
            return null;
        }
        //存放计算结果
        Stack<String> stack = new Stack<>();
        //存放方法函数
        Stack<String> functionStack = new Stack<>();
        //存放方法函数
        Stack<String> functionParamStack = new Stack<>();
        while (!expressQueue.isEmpty()) {
            //获取当前操作的内容
            String template = expressQueue.poll();
            //是运算方法特殊处理
            if (functionNames.containsKey(template.toLowerCase())) {
                //方法配对成功，获取入参进行运算
                if (!functionStack.isEmpty() && template.equals(functionStack.peek())) {
                    //方法名
                    String functionName = functionStack.pop();
                    while (true) {
                        //获取方法名和方法入参
                        String functionParam = stack.pop();
                        if (!functionParam.equals(functionName)) {
                            functionParamStack.push(functionParam);
                        } else {
                            break;
                        }
                    }
                    //对方法函数进行运算
                    stack.add(getFunctionReturn(functionName, functionParamStack).toString());
                    functionParamStack = new Stack<>();
                } else {
                    //没有配对方法，直接入栈
                    stack.push(template);
                    functionStack.push(template);
                }
            } else
                //如果是运算符进行运算
                if (isTrue(template)) {
                    BigDecimal num1 = new BigDecimal(stack.pop());
                    BigDecimal num2 = new BigDecimal(stack.pop());
                    stack.push(operation(num1, num2, template));
                    //如果不是运算符直接放入到运算结果中等待运算
                } else {
                    stack.push(template);
                }
        }
        //显示计算结果
        return new BigDecimal(stack.pop());
    }

    /**
     * 执行自定义函数运行结果
     *
     * @param functionName
     * @param functionParamStack
     * @return
     */
    private BigDecimal getFunctionReturn(String functionName, Stack functionParamStack) {
        try {

            //获取对应方法
            Method get = null;
            String methodName = functionNames.get(functionName.toLowerCase());

            // 如果有自定义函数类则优先使用自定义函数
            if (functionClass != null) {
                try {
                    get = functionClass.getDeclaredMethod(methodName, Stack.class);
                } catch (Exception ignored) {
                }
            }
            if (get == null) {
                get = Function.class.getDeclaredMethod(functionNames.get(functionName.toLowerCase()), Stack.class);
            }

            //获取方法返回值
            return (BigDecimal) get.invoke(null, functionParamStack);
        } catch (Exception e) {
            throw new CalculatorException.FunctionInExistence("The function doesn't exist  functionName: " + functionName, e);
        }
    }

    /**
     * 二元运算
     *
     * @param num1     值1
     * @param num2     值2
     * @param template 运算符
     * @return 计算结果
     */
    public String operation(BigDecimal num1, BigDecimal num2, String template) {
        BigDecimal num = BigDecimal.ZERO;
        if ("+".equals(template)) {
            num = num2.add(num1);
        } else if ("-".equals(template)) {
            num = num2.subtract(num1);
        } else if ("*".equals(template)) {
            num = num2.multiply(num1);
        } else if ("/".equals(template)) {
            num = num2.divide(num1, this.scale, BigDecimal.ROUND_HALF_UP);
        }
        return num.toString();
    }

    /**
     * 解析表达式
     *
     * @param equation
     */
    private void resolution(String equation) {
        //定义临时存储运算符的栈
        Stack<String> stack = new Stack<>();
        StringBuilder tempEquation = new StringBuilder();  // 记录当前运算符 及参数值
        for (int i = 0; i < equation.length(); i++) {
            tempEquation.append(equation, i, i + 1);

            // 判断当前字符为数字则跳过运算符入栈
            if (i < equation.length() - 1  // 当前长度总长度
                    && !isTrue(tempEquation.toString()) // 当前字符不是运算符
                    && !isTrue(equation.substring(i + 1, i + 2)) // 下一个字符不是运算符
                    || ((i == 0 || isTrue(equation.substring(i - 1, i))) // 上一个字符为运算符且不是‘）’ 或 当前为第一个字符 且本次为‘-’ 则表示为负数
                    && "-".contentEquals(tempEquation) && !")".equals(equation.substring(i - 1, i)))) {
                continue;
            }

            String temp = tempEquation.toString();
            tempEquation = new StringBuilder(); //清空Stringbuilder

            if (functionNames.containsKey(temp.toLowerCase())) {
                stack.push(temp);
                expressQueue.add(temp);
            } else if ("(".equals(temp)) {  //如果“(”直接入栈
                stack.push(temp);
                //如果“）”直接入栈，运算符出栈进入队列，直到遇到“（”，并去除“（”
            } else if (")".equals(temp)) {
                while (true) {
                    if ("(".equals(stack.peek())) {
                        stack.pop();
                        if (functionNames.containsKey(stack.peek().toLowerCase())) {
                            expressQueue.add(stack.pop());
                        }
                        break;
                    }
                    //","只用来判断，不加入到后缀表达式中
                    String operatorString = stack.pop();
                    if (!",".equals(operatorString)) {
                        expressQueue.add(operatorString);
                    }
                }
            } else if (isTrue(temp)) {
                //如果是普通运算符，将运算优先级大于等于他的从栈中取出加入到队列中，最后将当前运算符入栈
                while (true) {
                    if (!stack.isEmpty() && getPriority(temp) <= getPriority(stack.peek())) {
                        //","只用来判断，不加入到后缀表达式中
                        String operatorString = stack.pop();
                        if (!",".equals(operatorString)) {
                            expressQueue.add(operatorString);
                        }
                    } else {
                        break;
                    }
                }
                //兼容-1
                if (expressQueue.isEmpty() && "-".equals(temp)) {
                    expressQueue.add("0");
                }
                stack.push(temp);

            } else {
                //如果是数字直接进入队列
                expressQueue.add(temp);
            }
        }
        //将剩余的运算符加入到队列中
        if (!stack.isEmpty()) {
            while (!stack.isEmpty()) {
                expressQueue.add(stack.pop());
            }
        }
    }

    /**
     * 运算符判断
     *
     * @param temp 运算符
     * @return
     */
    private int getPriority(String temp) {
        if ("+".equals(temp) || "-".equals(temp)) {
            return 1;
        } else if ("*".equals(temp) || "/".equals(temp)) {
            return 2;
        } else if (",".equals(temp)) {
            return 0;
        } else {
            return -2;
        }
    }

    /**
     * 判断是不是特殊符号
     *
     * @param temp 符号
     * @return
     */
    private boolean isTrue(String temp) {
        if ("+".equals(temp) || "-".equals(temp) || "*".equals(temp) || "/".equals(temp) || ",".equals(temp) || "(".equals(temp) || ")".equals(temp)) {
            return true;
        }
        return false;
    }


    /**
     * 默认函数实现函数
     */
    private final static class Function {
        /**
         * 判断boolean 表达式结果
         *
         * @param logical_test
         * @return
         */
        private static boolean isTrue(String logical_test) {
            if (logical_test.contains("==")) {
                String[] test = logical_test.split("==");
                return test[0].equals(test[1]);
            } else if (logical_test.contains("true") || logical_test.contains("false")) {
                return Boolean.parseBoolean(logical_test);
            } else if (logical_test.contains("<>") || logical_test.contains("><") || logical_test.contains("!=")) {
                String[] test = logical_test.split(logical_test.contains("<>") ? "<>" : logical_test.contains("><") ? "><" : "!=");
                return !test[0].equals(test[1]);
            } else if (logical_test.contains("<")) {
                String[] test = logical_test.split("<");
                return new BigDecimal(test[0]).compareTo(new BigDecimal(test[1])) < 0;
            } else if (logical_test.contains(">")) {
                String[] test = logical_test.split(">");
                return new BigDecimal(test[0]).compareTo(new BigDecimal(test[1])) > 0;

            } else if (logical_test.equals("1")) {
                return true;
            } else if (logical_test.equals("0")) {
                return false;
            }
            return false;
        }


        /**
         * if函数支持  三元表达式  Excel使用方式
         *
         * @param stack
         * @return
         */
        public static BigDecimal IF(Stack<String> stack) {
            String logical_test = stack.pop();
            String val1 = stack.pop();
            String val2 = stack.pop();
            return isTrue(logical_test) ? new BigDecimal(val1) : new BigDecimal(val2);
        }

        /**
         * 条件或
         *
         * @param stack
         * @return true 返回1  false返回0
         */
        public static BigDecimal or(Stack<String> stack) {
            while (!stack.empty()) {
                if (isTrue(stack.pop())) return BigDecimal.ONE;
            }
            return BigDecimal.ZERO;
        }

        public static BigDecimal and(Stack<String> stack) {
            while (!stack.empty()) {
                if (!isTrue(stack.pop())) return BigDecimal.ZERO;
            }
            return BigDecimal.ONE;
        }

        /**
         * 取整  四舍五入
         *
         * @param stack
         * @return
         */
        public static BigDecimal round(Stack<String> stack) {
            if (stack == null || stack.size() != 2) {
                throw new CalculatorException.FunctionRun("Round function parameter error");
            }
            return new BigDecimal(stack.pop()).setScale(Integer.parseInt(stack.pop()), BigDecimal.ROUND_HALF_UP);
        }

        /**
         * 取整  向上
         *
         * @param stack
         * @return
         */
        public static BigDecimal roundup(Stack<String> stack) {
            if (stack == null || stack.size() != 2) {
                throw new CalculatorException.FunctionRun("Round function parameter error");
            }
            return new BigDecimal(stack.pop()).setScale(Integer.parseInt(stack.pop()), BigDecimal.ROUND_UP);
        }

        /**
         * 取整  向下
         *
         * @param stack
         * @return
         */
        public static BigDecimal roundDown(Stack<String> stack) {
            if (stack == null || stack.size() != 2) {
                throw new CalculatorException.FunctionRun("Round function parameter error");
            }
            return new BigDecimal(stack.pop()).setScale(Integer.parseInt(stack.pop()), BigDecimal.ROUND_DOWN);
        }

        /**
         * 求和
         *
         * @param stack
         * @return true 返回1  false返回0
         */
        public static BigDecimal sum(Stack<String> stack) {
//            BigDecimal sum = new BigDecimal("0");
//            while (!stack.isEmpty()) {
//                sum = sum.add(new BigDecimal(stack.pop()));
//            }
//            return sum;
            return stack.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        /**
         * 求平均值
         *
         * @param stack
         * @return
         */
        public static BigDecimal avg(Stack<String> stack) {
//            BigDecimal sum = new BigDecimal("0");
//            int count = stack.size();
//            while (!stack.isEmpty()) {
//                sum = sum.add(new BigDecimal(stack.pop()));
//            }
//            return sum.divide(new BigDecimal(count), 10, BigDecimal.ROUND_HALF_UP);
            return new BigDecimal(stack.stream().mapToDouble(i -> new BigDecimal(i).doubleValue()).average().orElse(0.0));
        }

        /**
         * 最大值
         *
         * @param stack
         * @return
         */
        public static BigDecimal max(Stack<String> stack) {
//            BigDecimal max = BigDecimal.ZERO;
//            while (!stack.isEmpty()) {
//                BigDecimal next = new BigDecimal(stack.pop());
//                if (max.compareTo(next) < 0) max = next;
//            }
//            return max;
            return stack.stream().map(BigDecimal::new).max(Comparator.comparing(BigDecimal::doubleValue)).orElse(BigDecimal.ZERO);
        }

        /**
         * min
         *
         * @param stack
         * @return
         */
        public static BigDecimal min(Stack<String> stack) {
//            BigDecimal min = new BigDecimal(stack.pop());
//            while (!stack.isEmpty()) {
//                BigDecimal next = new BigDecimal(stack.pop());
//                if (min.compareTo(next) > 0) min = next;
//            }
//            return min;
            return stack.stream().map(BigDecimal::new).min(Comparator.comparing(BigDecimal::doubleValue)).orElse(BigDecimal.ZERO);
        }

        /**
         * count
         *
         * @param stack
         * @return
         */
        public static BigDecimal count(Stack<String> stack) {
            if (stack != null) return new BigDecimal(stack.size());
            return BigDecimal.ZERO;
        }
    }
}
