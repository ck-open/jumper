package com.ck.check_bean;

import com.ck.check_bean.annotation.CheckValue;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @ClassName TestCheckValueUtil
 * @Author Cyk
 * @Version 1.0
 * @since 2023/2/23 16:24
 **/
public class TestCheckValueUtil {
    /**
     * 打印对象属性列表
     */
    public static void printParameters(Class cls) {
        if (cls == null) {
            System.out.println("类型为null");
        } else {
            for (Field field : CheckValueUtil.getFields(cls)) {
                CheckValue checkValue = field.getAnnotation(CheckValue.class);
                String msg = checkValue == null || "".equalsIgnoreCase(checkValue.value()) ? "" : "  // " + checkValue.value();
                System.out.println("\"" + cls.getSimpleName() + "." + field.getName() + "\", " + msg);
            }
        }
    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.setItem(new DemoItem());

        demo.setDoub("-25.67");
        demo.setStrTime("2022-08-04 18:42:35");
        List<CheckResult> msg = CheckValueUtil.checkBeanFieldIsNotNull(demo, "S");

        System.out.println(msg);

    }

    static class Demo {
        @CheckValue(value = "测试字符串", flag = {"P"})
        private String str;
        @CheckValue(value = "测试日期", regexp = "^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})$", flag = {"P"})
        private String strTime;
        @CheckValue("测试数字")
        private Integer inte;
        @CheckValue(value = "测试小数", max = 50, min = -25.66)
        private String doub;
        @CheckValue(value = "测试子对象", flag = {"P", "E"}, isChild = true)
        private DemoItem item;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public Integer getInte() {
            return inte;
        }

        public void setInte(Integer inte) {
            this.inte = inte;
        }

        public DemoItem getItem() {
            return item;
        }

        public void setItem(DemoItem item) {
            this.item = item;
        }

        public String getDoub() {
            return doub;
        }

        public void setDoub(String doub) {
            this.doub = doub;
        }

        public String getStrTime() {
            return strTime;
        }

        public void setStrTime(String strTime) {
            this.strTime = strTime;
        }
    }

    static class DemoItem {
        @CheckValue("子对象字符")
        private String strItem;
        @CheckValue(value = "子对象数字", flag = {"P", "E"}, isOptional = true)
        private Integer inteItem;

        public String getStrItem() {
            return strItem;
        }

        public void setStrItem(String strItem) {
            this.strItem = strItem;
        }

        public Integer getInteItem() {
            return inteItem;
        }

        public void setInteItem(Integer inteItem) {
            this.inteItem = inteItem;
        }
    }
}
