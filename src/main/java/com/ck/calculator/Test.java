package com.ck.calculator;

import java.math.BigDecimal;
import java.util.Stack;

class Test {
    public static void main(String[] args) {
        System.out.println(Calculator.getTag(null));

        String equation = "{}if(or(24==24,89>100),roundDown(9+avg(3,1,sum(1,3)-sum(2,4),7*3)+8/2,2),round(-265/23,2))";//19
//        String equation = "23--5+15";//19
//        String equation = "max(25,15,62,70)+min(8,-9,5,3)+count(8,5,46,5,48,2)";//19

        //定义公式解析器
        Calculator calculator = new Calculator();

        //传入自定义运算函数类
        calculator.setFunctionClass(A.class);

        long millis = System.currentTimeMillis();

//        for (int i=0; i<10000;i++){
            //计算值
            BigDecimal result = calculator.calculate(equation);
//        }

        System.out.println("总耗时："+(System.currentTimeMillis()-millis));


        //计算值
//        BigDecimal result = calculator.calculate(equation);
//        System.out.println(result);
    }

    public static class A{
        /**
         * min
         *
         * @param stack
         * @return
         */
        public static BigDecimal mina(Stack<String> stack) {
            BigDecimal min = new BigDecimal(stack.pop());
            while (!stack.isEmpty()) {
                BigDecimal next = new BigDecimal(stack.pop());
                if (min.compareTo(next) > 0) min = next;
            }
            return min;
        }

    }
}
