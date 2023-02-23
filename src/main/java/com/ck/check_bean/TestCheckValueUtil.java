package com.ck.check_bean;

import com.alibaba.fastjson.JSONObject;
import com.ck.check_bean.annotation.CheckValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
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
        demo.getItem().setInteItem(658);
//        demo.getItem().setStrItem("sdfds");
        demo.setItems(Arrays.asList(new DemoItem(),new DemoItem()));

        demo.setDoub("-25.67");
        demo.setStrTime("2022-08-04 18:42:35");
        List<CheckResult> msg = CheckValueUtil.checkBeanFieldIsNotNull(demo, "S");

        CheckItem checkItem = new CheckItem();
        checkItem.setCheckItemChild(new HashMap<>());
        checkItem.getCheckItemChild().put("str",new CheckItem().setValue("测试字符串").setFlag(new String[]{"P"}));
        checkItem.getCheckItemChild().put("inte",new CheckItem().setValue("测试数字").setFlag(new String[]{"P"}));
        checkItem.getCheckItemChild().put("strTime",new CheckItem().setValue("测试日期").setFlag(new String[]{"P"}).setRegexp("^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})$"));
        checkItem.getCheckItemChild().put("doub",new CheckItem().setValue("测试小数").setMax(50.00).setMin(-25.66));
        checkItem.getCheckItemChild().put("item",new CheckItem().setValue("测试子对象").setFlag(new String[]{"P", "E"}).setChild(true).setCheckItemChild(new HashMap<>()));

        checkItem.getCheckItemChild().get("item").getCheckItemChild().put("strItem",new CheckItem().setValue("子对象字符"));
        checkItem.getCheckItemChild().get("item").getCheckItemChild().put("inteItem",new CheckItem().setValue("子对象数字"));
        List<CheckResult> checkResult = CheckValueUtil.checkBeanFieldIsNotNull(JSONObject.parseObject(JSONObject.toJSONString(demo)),null,checkItem);

        System.out.println(msg);

    }

    @Data
    @Accessors(chain = true)
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
        @CheckValue(value = "测试子对象列表", flag = {"P", "E"}, isChild = true)
        private List<DemoItem> items;

    }
    @Data
    @Accessors(chain = true)
    static class DemoItem {
        @CheckValue("子对象字符")
        private String strItem;
        @CheckValue(value = "子对象数字", flag = {"P", "E"}, isOptional = true)
        private Integer inteItem;
    }
}
