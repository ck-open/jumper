package com.ck.calculator;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

class Test {
    public static void main(String[] args) {
        //要计算的公式
        //认为公式为正确的不需要校验
        String equation = "if(and(25==25,89>100),round(9+avg(3,1,sum(1,3)-sum(2,4),7)*3+8/2,2),666)";//19
        //定义公式解析器
        //传入运算函数所在的类
        Calculator calculator = new Calculator();

        //计算值
        BigDecimal result = calculator.calculate(equation);
        System.out.println(result);
    }
}

public class Calculator {
    private Logger log = Logger.getLogger(Calculator.class.getName());

    // 表达式队列
    private Queue<String> expressQueue = new ArrayDeque<>();

    //自定义的函数类
    private Class<?> functionClass = null;
    //支持的函数名列表
//    private Set<String> functionNames = new HashSet<>();
    private Map<String, String> functionNames = new HashMap<>();

    public Calculator() {
        this.setFunctionClass(Function.class);
    }

    /**
     * 设置自定义函数类
     */
    public void setFunctionClass(Class<? super Function> functionClass) {
        if (functionClass != null) {
            this.functionClass = functionClass;

            // 记录所有以实现的函数名列表
            for (Method m : this.functionClass.getDeclaredMethods()) {
//                this.functionNames.add(m.getName());
                this.functionNames.put(m.getName().toLowerCase(), m.getName());
            }
        }
    }


    /**
     * 执行计算
     *
     * @param equation
     * @return
     */
    public BigDecimal calculate(String equation) {
        resolution(equation);
        return calculate();
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
        Stack functionParamStack = new Stack();
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
                        if (functionParam != functionName) {
                            functionParamStack.push(functionParam);
                        } else {
                            break;
                        }
                    }
                    //对方法函数进行运算
                    stack.add(getFunctionReturn(functionName, functionParamStack).toString());
                    functionParamStack = new Stack();
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
            Method get = functionClass.getDeclaredMethod(functionNames.get(functionName.toLowerCase()), Stack.class);
            //获取方法返回值
            return (BigDecimal) get.invoke(null, functionParamStack);
        } catch (Exception e) {
            log.warning("The function doesn't exist  functionName:" + functionNames.get(functionName.toLowerCase()));
            throw new CalculatorException.FunctionInExistence(e);
        }
    }

    /**
     * 二元运算
     *
     * @param num1
     * @param num2
     * @param template
     * @return
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
            num = num2.divide(num1);
        }
        return num.toString();
    }

    /**
     * 中缀转后缀表达式
     *
     * @param equation
     */
    public void resolution(String equation) {
        //定义临时存储运算符的栈
        Stack<String> stack = new Stack<>();
        StringBuilder tempStringBuilder = new StringBuilder();
        for (int i = 0; i < equation.length(); i++) {
            tempStringBuilder.append(equation, i, i + 1);
            if (i + 2 <= equation.length() && !isTrue(equation.substring(i + 1, i + 2)) && !isTrue(tempStringBuilder.toString())) {
                continue;
            }
            String temp = tempStringBuilder.toString();
            //清空Stringbuilder
            tempStringBuilder = new StringBuilder();
            //如果“(”直接入栈
            if (functionNames.containsKey(temp.toLowerCase())) {
                stack.push(temp);
                expressQueue.add(temp);

            } else if ("(".equals(temp)) {
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
                    String operatorString = stack.pop().toString();
                    if (!",".equals(operatorString)) {
                        expressQueue.add(operatorString);
                    }
                }
            } else if (isTrue(temp)) {
                //如果是普通运算符，将运算优先级大于等于他的从栈中取出加入到队列中，最后将当前运算符入栈
                while (true) {
                    if (!stack.isEmpty() && getPriority(temp) <= getPriority(stack.peek())) {
                        //","只用来判断，不加入到后缀表达式中
                        String operatorString = stack.pop().toString();
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

    public int getPriority(String temp) {
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
     * @param temp
     * @return
     */
    public boolean isTrue(String temp) {
        if ("+".equals(temp) || "-".equals(temp) || "*".equals(temp) || "/".equals(temp) || ",".equals(temp) || "(".equals(temp) || ")".equals(temp)) {
            return true;
        }
        return false;
    }


    /**
     * 函数计算器父类
     */
    public static class Function {
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
            } else if (logical_test.contains("<>") || logical_test.contains("!=")) {
                String[] test = logical_test.split(logical_test.contains("<>") ? "<>" : "!=");
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
        public static BigDecimal IF(Stack stack) {
            return isTrue(stack.pop().toString()) ? new BigDecimal(stack.pop().toString()) : new BigDecimal(stack.pop().toString());
        }

        /**
         * 条件或
         *
         * @param stack
         * @return true 返回1  false返回0
         */
        public static BigDecimal OR(Stack stack) {
            while (stack.empty()) {
                if (isTrue(stack.pop().toString())) return BigDecimal.ONE;
            }
            return BigDecimal.ZERO;
        }

        public static BigDecimal AND(Stack stack) {
            while (!stack.empty()) {
                if (!isTrue(stack.pop().toString())) return BigDecimal.ZERO;
            }
            return BigDecimal.ONE;
        }


        /**
         * 求和
         *
         * @param stack
         * @return true 返回1  false返回0
         */
        public static BigDecimal sum(Stack stack) {
            BigDecimal sum = new BigDecimal("0");
            while (!stack.isEmpty()) {
                sum = sum.add(new BigDecimal(stack.pop().toString()));
            }
            return sum;
        }

        /**
         * 求平均值
         *
         * @param stack
         * @return
         */
        public static BigDecimal avg(Stack stack) {
            BigDecimal sum = new BigDecimal("0");
            int count = stack.size();
            while (!stack.isEmpty()) {
                sum = sum.add(new BigDecimal(stack.pop().toString()));
            }
            return sum.divide(new BigDecimal(count), 7, BigDecimal.ROUND_HALF_UP);
        }

        /**
         * 取整
         *
         * @param stack
         * @return
         */
        public static BigDecimal round(Stack stack) {
            if (stack == null || stack.size() != 2) {
                throw new CalculatorException.FunctionRun("Round function parameter error");
            }
            return new BigDecimal(stack.pop().toString()).setScale(Integer.parseInt(stack.pop().toString()), BigDecimal.ROUND_HALF_UP);
        }


    }
}