package com.ck.utils;

import java.text.NumberFormat;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公式运算
 */
public final class CalculateUtil {
    private static Logger log = Logger.getLogger(CalculateUtil.class.getName());

    private static double doubleCal(double a1, double a2, char operator) throws Exception {
        switch (operator) {
            case '+':
                return a1 + a2;
            case '-':
                return a1 - a2;
            case '*':
                return a1 * a2;
            case '/':
                return a1 / a2;
            default:
                break;
        }
        throw new Exception("illegal operator!");
    }

    private static int getPriority(String s) throws Exception {
        if (s == null) return 0;
        else if (s.equals("(")) return 1;
        else if (s.equals("+") || s.equals("-")) return 2;
        else if (s.equals("*") || s.equals("/")) return 3;
        
        /*
        switch(s) {
        case "(":return 1;
        case "+":;
        case "-":return 2;
        case "*":;
        case "/":return 3;
        default:break;
        }
        */

        throw new Exception("illegal operator!");
    }

    /**
     * 执行计算
     * @param expr
     * @return
     * @throws Exception
     */
    public static String calculate(String expr) throws Exception {
        log.info("计算==> " + expr);
        /*数字栈*/
        Stack<Double> number = new Stack<Double>();
        /*符号栈*/
        Stack<String> operator = new Stack<String>();
        operator.push(null);// 在栈顶压人一个null，配合它的优先级，目的是减少下面程序的判断

        /* 将expr打散为运算数和运算符 */
        Pattern p = Pattern.compile("(?<!\\d)-?\\d+(\\.\\d+)?|[+\\-*/()]");// 这个正则为匹配表达式中的数字或运算符
        Matcher m = p.matcher(expr);
        while (m.find()) {
            String temp = m.group();
            if (temp.matches("[+\\-*/()]")) {//遇到符号
                if (temp.equals("(")) {//遇到左括号，直接入符号栈
                    operator.push(temp);
                    //System.out.println("符号栈更新："+operator);
                } else if (temp.equals(")")) {//遇到右括号，"符号栈弹栈取栈顶符号b，数字栈弹栈取栈顶数字a1，数字栈弹栈取栈顶数字a2，计算a2 b a1 ,将结果压入数字栈"，重复引号步骤至取栈顶为左括号，将左括号弹出
                    String b = null;
                    while (!(b = operator.pop()).equals("(")) {
                        //System.out.println("符号栈更新："+operator);
                        double a1 = number.pop();
                        double a2 = number.pop();
                        // System.out.println("数字栈更新："+number);
                        //System.out.println("计算"+a2+b+a1);
                        number.push(doubleCal(a2, a1, b.charAt(0)));
                        //System.out.println("数字栈更新："+number);
                    }
                    //System.out.println("符号栈更新："+operator);
                } else {//遇到运算符，满足该运算符的优先级大于栈顶元素的优先级压栈；否则计算后压栈
                    while (getPriority(temp) <= getPriority(operator.peek())) {
                        double a1 = number.pop();
                        double a2 = number.pop();
                        String b = operator.pop();
                        //System.out.println("符号栈更新："+operator);
                        //System.out.println("数字栈更新："+number);
                        //System.out.println("计算"+a2+b+a1);
                        number.push(doubleCal(a2, a1, b.charAt(0)));
                        //System.out.println("数字栈更新："+number);
                    }
                    operator.push(temp);
                    //System.out.println("符号栈更新："+operator);
                }
            } else {//遇到数字，直接压入数字栈
                number.push(Double.valueOf(temp));
                //System.out.println("数字栈更新："+number);
            }
        }

        while (operator.peek() != null) {//遍历结束后，符号栈数字栈依次弹栈计算，并将结果压入数字栈
            double a1 = number.pop();
            double a2 = number.pop();
            String b = operator.pop();
            //System.out.println("符号栈更新："+operator);
            //System.out.println("数字栈更新："+number);
            //System.out.println("计算"+a2+b+a1);
            number.push(doubleCal(a2, a1, b.charAt(0)));
            //System.out.println("数字栈更新："+number);
        }
        // System.out.println("值="+number.pop()+"");
        Double pop = number.pop();//科学计数
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        String format = nf.format(pop);//科学计数转换成完全显示
        return format;
        // return number.pop()+"";
    }


    public static void main(String[] args) throws Exception {
        String str = "-3.5*(4.5-(4+(-1-1/2)))";
        str = "(1.4-1)*3-5*(2.1-1)";
        System.out.println(calculate(str));


    }


}