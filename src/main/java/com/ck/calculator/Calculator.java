package com.ck.calculator;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

class Test {
    public static void main(String[] args) {
        //要计算的公式
        //认为公式为正确的不需要校验
        String equation = "9+avg(3,1,sum(1,3)-sum(2,4),7)*3+8/2";//19
        //定义公式解析器
        //传入运算函数所在的类
        Calculator calculator = new Calculator(Test.class);
        //添加计算方法
        //求和
        calculator.addFunction("sum");
        //求平均值
        calculator.addFunction("avg");
        //计算值
        BigDecimal result = calculator.calculate(equation);
        System.out.println(result);
    }

    Test() {
    }

    /**
     * 求和
     *
     * @param stack
     * @return
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
}

public class Calculator {
    //存放方法
    Set functionList = new HashSet();
    //存放后序表达式
    Queue queue = new ArrayDeque();
    //含有计算方法的class
    Class functionClass = null;

    /**
     * 添加方法
     */
    public Calculator(Class functionClass) {
        this.functionClass = functionClass;
    }

    public void addFunction(String funName) {
        functionList.add(funName);
    }

    public BigDecimal calculate(String equation) {
        resolution(equation);
        return calculate();
    }

    public BigDecimal calculate() {
        if (queue.isEmpty()) {
            return null;
        }
        //存放计算结果
        Stack stack = new Stack();
        //存放方法函数
        Stack functionStack = new Stack();
        //存放方法函数
        Stack functionParamStack = new Stack();
        while (!queue.isEmpty()) {
            //获取当前操作的内容
            String template = (String) queue.poll();
            //是运算方法特殊处理
            if (functionList.contains(template)) {
                //方法配对成功，获取入参进行运算
                if (!functionStack.isEmpty() && template.equals(functionStack.peek())) {
                    //方法名
                    String functionName = (String) functionStack.pop();
                    while (true) {
                        //获取方法名和方法入参
                        String functionParam = (String) stack.pop();
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
                    BigDecimal num1 = new BigDecimal((String) stack.pop());
                    BigDecimal num2 = new BigDecimal((String) stack.pop());
                    stack.push(operation(num1, num2, template));
                    //如果不是运算符直接放入到运算结果中等待运算
                } else {
                    stack.push(template);
                }
        }
        //显示计算结果
        return new BigDecimal(stack.pop().toString());
    }

    /**
     * 执行自定义函数运行结果
     * @param functionName
     * @param functionParamStack
     * @return
     */
    private BigDecimal getFunctionReturn(String functionName, Stack functionParamStack) {
        BigDecimal result = new BigDecimal("1");
        try {
            //获取对应方法
            Method get = functionClass.getMethod(functionName, Stack.class);
            //获取方法返回值
            result = (BigDecimal) get.invoke(null, (Object) functionParamStack);
            return result;
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * 两个数进行运算
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
        Stack stack = new Stack();
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
            if (functionList.contains(temp)) {
                stack.push(temp);
                queue.add(temp);

            } else if ("(".equals(temp)) {
                stack.push(temp);
                //如果“）”直接入栈，运算符出栈进入队列，直到遇到“（”，并去除“（”
            } else if (")".equals(temp)) {
                while (true) {
                    if ("(".equals(stack.peek())) {
                        stack.pop();
                        if (functionList.contains(stack.peek())) {
                            queue.add(stack.pop());
                        }
                        break;
                    }
                    //","只用来判断，不加入到后缀表达式中
                    String operatorString = stack.pop().toString();
                    if (!",".equals(operatorString)) {
                        queue.add(operatorString);
                    }
                }
            } else if (isTrue(temp.toString())) {
                //如果是普通运算符，将运算优先级大于等于他的从栈中取出加入到队列中，最后将当前运算符入栈
                while (true) {
                    if (!stack.isEmpty() && getPriority(temp.toString()) <= getPriority((String) stack.peek())) {
                        //","只用来判断，不加入到后缀表达式中
                        String operatorString = stack.pop().toString();
                        if (!",".equals(operatorString)) {
                            queue.add(operatorString);
                        }
                    } else {
                        break;
                    }
                }
                //兼容-1
                if (queue.isEmpty() && "-".equals(temp)) {
                    queue.add("0");
                }
                stack.push(temp);

            } else {
                //如果是数字直接进入队列
                queue.add(temp);
            }
        }
        //将剩余的运算符加入到队列中
        if (!stack.isEmpty()) {
            while (!stack.isEmpty()) {
                queue.add(stack.pop());
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
}