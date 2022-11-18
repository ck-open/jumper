package com.ck.calculator;

import java.math.BigDecimal;

class Test {
    public static void main(String[] args) {
        //要计算的公式
        //认为公式为正确的不需要校验
//        String equation = "if(or(24==24,89>100),roundDown(9+avg(3,1,sum(1,3)-sum(2,4),7*3)+8/2,2),round(-265/23,2))";//19
//        String equation = "23--5+15";//19
        String equation = "max(25,15,62,70)+min(8,9,5,3)+count(8,5,46,5,48,2)";//19
        //定义公式解析器
        //传入运算函数所在的类
        Calculator calculator = new Calculator();

        //计算值
        BigDecimal result = calculator.calculate(equation);
        System.out.println(result);
    }
}
