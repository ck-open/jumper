//package com.ck.check_bean;
//
//import com.ck.check_bean.annotation.CheckValue;
//
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.util.*;
//
//
///**
// * 实体对象属性非空校验工具
// *
// * @author cyk
// * @since 2021-06-06
// */
//public final class CheckValueUtil {
//
//    /**
//     * 递归非空与正则校验
//     *
//     * @param o 校验的对象
//     * @return
//     */
//    public static List<CheckResult> checkBeanFieldIsNotNull(Object o) {
//        return checkBeanFieldIsNotNull(o, null, null, null, null);
//    }
//
//    /**
//     * 递归非空与正则校验
//     *
//     * @param o    校验的对象
//     * @param flag 自定义是否校验标签
//     * @return
//     */
//    public static List<CheckResult> checkBeanFieldIsNotNull(Object o, String flag) {
//        return checkBeanFieldIsNotNull(o, null, null, null, flag);
//    }
//
//    /**
//     * 递归非空与正则校验
//     *
//     * @param o        校验的对象
//     * @param fields   需要校验的参数列表，例如：BaseRequestDto.orderId
//     * @param regexMap 正则字典：key为 ClassSimpleNem.FieldNme  例如：BaseRequestDto.orderId
//     * @return
//     */
//    public static List<CheckResult> checkBeanFieldIsNotNull(Object o, List<String> fields, Map<String, String> fieldNames, Map<String, String> regexMap) {
//        return checkBeanFieldIsNotNull(o, fields, regexMap, fieldNames, null);
//    }
//
//    /**
//     * 递归非空与正则校验
//     *
//     * @param o          校验的对象
//     * @param fields     需要校验的参数列表，例如：BaseRequestDto.orderId
//     * @param fieldNames 需要校验的参数列表，key：BaseRequestDto.orderId
//     * @param regexMap   正则字典：key为 ClassSimpleNem.FieldNme  例如：BaseRequestDto.orderId
//     * @param flag       自定义是否校验标签
//     * @return
//     */
//    private static List<CheckResult> checkBeanFieldIsNotNull(Object o, List<String> fields, Map<String, String> fieldNames, Map<String, String> regexMap, String flag) {
//        List<CheckResult> result = new ArrayList<>();
//        if (fields == null) {
//            fields = new ArrayList<>();
//        }
//        if (fieldNames == null) {
//            fieldNames = new HashMap<>();
//        }
//        if (regexMap == null) {
//            regexMap = new HashMap<>();
//        }
//
//        Class cla = o.getClass();
//        for (Field field : getFields(cla)) {
//            String key = cla.getSimpleName() + "." + field.getName();
//
//            String msg = key;
//            String regex = regexMap.get(key);
//            CheckValue checkValue = field.getAnnotation(CheckValue.class);
//            if (checkValue != null) {
//                msg = fieldNames.containsKey(key) ? fieldNames.get(key) : !"".equalsIgnoreCase(checkValue.value()) ? checkValue.value() : msg;
//                if (regex == null) regex = checkValue.regexp();
//            }
//
//            // 无注解 也未配置 则跳过校验
//            if (fields.contains(key) || checkValue != null) {
//                Object val = getVal(field, o, key);
//                if (val != null && "".equals(val.toString().trim())) {
//                    val = null;
//                }
//
//                CheckResult checkResult = new CheckResult().setCla(cla).setField(field).setValue(o);
//
//                if (val == null) {
//                    if (checkValue != null && !"".equalsIgnoreCase(checkValue.defaultValue())) {
//                        setVal(field, o, checkValue.defaultValue(), key);
//
//                        // 未注解 或 必传属性 或 flag标记中包含指定的标记则校验非空 否则跳过
//                    } else if (checkValue == null || !checkValue.isOptional()
//                            || (flag != null && !"".equals(flag.trim()) && checkValue.flag().length > 0 && Arrays.asList(checkValue.flag()).contains(flag))) {
//                        result.add(checkResult.setMessage(msg + "为空"));
//                    }
//                } else {
//                    if (!checkRegex(val, regex)) {
//                        checkResult.setRegexp(regex);
//                        result.add(checkResult.setMessage(msg + "规则不符"));
//                        continue;
//                    } else if (checkValue != null) {
//                        if (checkValue.max() != -999999999 && !checkMax(checkValue.max(), val, key)) {
//                            checkResult.setMax(checkValue.max());
//                            result.add(checkResult.setMessage(msg + String.format("大于限定值[%s]", checkValue.max())));
//                            continue;
//                        }
//                        if (checkValue.min() != -999999999 && !checkMin(checkValue.max(), val, key)) {
//                            checkResult.setMin(checkValue.min());
//                            result.add(checkResult.setMessage(msg + String.format("小于限定值[%s]", checkValue.min())));
//                            continue;
//                        }
//                    }
//
//                    // 是否递归校验
//                    if (!isBasicType(val)) {
//                        if (checkValue != null && !checkValue.isChild()) {
//                            continue;
//                        }
//                        result.addAll(recursion(val, fields, fieldNames, regexMap, flag));
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 获取属性值
//     *
//     * @param field
//     * @param o
//     * @return
//     */
//    private static Object getVal(Field field, Object o, String fieldName) {
//        try {
//            field.setAccessible(true);
//            return field.get(o);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(String.format("获取属性值异常，属性:%s ", fieldName));
//        }
//    }
//
//    /**
//     * 为属性赋默认值
//     *
//     * @param field
//     * @param o
//     * @param defaultValue
//     * @param fieldName
//     */
//    private static void setVal(Field field, Object o, String defaultValue, String fieldName) {
//        try {
//            if (field.getType().getSimpleName().equals(String.class.getSimpleName())) {
//                field.set(o, defaultValue);
//            } else if (field.getType().getSimpleName().equals(Integer.class.getSimpleName())) {
//                field.set(o, Integer.valueOf(defaultValue));
//            } else if (field.getType().getSimpleName().equals(Double.class.getSimpleName())) {
//                field.set(o, Double.valueOf(defaultValue));
//            } else if (field.getType().getSimpleName().equals(BigDecimal.class.getSimpleName())) {
//                field.set(o, new BigDecimal(defaultValue));
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(String.format("为属性[%s]赋默认值[%s]异常, 需要的类型为[%s]！", fieldName, defaultValue, field.getType().getSimpleName()));
//        }
//    }
//
//    /**
//     * 最大值校验
//     *
//     * @param max
//     * @param val
//     * @return
//     */
//    private static boolean checkMax(double max, Object val, String fieldName) {
//        if (RegExUtil.checkNumber(String.valueOf(val))) {
//            if (max < Double.parseDouble(String.valueOf(val))) {
//                return false;
//            }
//        } else {
//            throw new RuntimeException(String.format("最大值只能用于数字类型校验,参数：%s  值：%s", fieldName, String.valueOf(val)));
//        }
//        return true;
//    }
//
//    /**
//     * 最小值校验
//     *
//     * @param min
//     * @param val
//     * @return
//     */
//    private static boolean checkMin(double min, Object val, String fieldName) {
//        if (RegExUtil.checkNumber(String.valueOf(val))) {
//            if (min > Double.parseDouble(String.valueOf(val))) {
//                return false;
//            }
//        } else {
//            throw new RuntimeException(String.format("最小值只能用于数字类型校验,参数：%s  值：%s", fieldName, String.valueOf(val)));
//        }
//        return true;
//    }
//
//    /**
//     * 递归属性值
//     *
//     * @param val
//     * @param checkParameters
//     * @param regex
//     * @return
//     */
//    private static List<CheckResult> recursion(Object val, List<String> checkParameters, Map<String, String> fieldNames, Map<String, String> regex, String flag) {
//        List<CheckResult> result = new ArrayList<>();
//        // 参数是集合
//        if (Collection.class.isAssignableFrom(val.getClass())) {
//            for (Object item : (Collection) val) {
//                result.addAll(checkBeanFieldIsNotNull(item, checkParameters, fieldNames, regex, flag));
//            }
//        } else if (val.getClass().isArray()) {   // 参数是数组
//            for (Object item : (Object[]) val) {
//                result.addAll(checkBeanFieldIsNotNull(item, checkParameters, fieldNames, regex, flag));
//            }
//        } else if (Map.class.isAssignableFrom(val.getClass())) {
//            for (Object item : ((Map) val).values()) {
//                result.addAll(checkBeanFieldIsNotNull(item, checkParameters, fieldNames, regex, flag));
//            }
//        } else {
//            result.addAll(checkBeanFieldIsNotNull(val, checkParameters, fieldNames, regex, flag));
//        }
//        return result;
//    }
//
//    /**
//     * 判断对象是否基本类型或常见类型
//     */
//    public static boolean isBasicType(Object o) {
//        if (o != null) {
//            // 基本类型不递归
//            if (isBasicClass(o)) {
//                return true;
//            }
//
//            // 以下类型不进行递归
//            List<String> types = Arrays.asList("String", "Date", "Calendar", "LocalTime", "BigDecimal", "BigInteger");
//            if (types.contains(o.getClass().getSimpleName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 按表达式检查数据对象是否合法
//     * <p>值判断基本类型，非基本类型直接返回true</p>
//     *
//     * @param val   值对象
//     * @param regex 正则表达式
//     * @return
//     */
//    public static boolean checkRegex(Object val, String regex) {
//        if (val != null && regex != null && !"".equals(regex.trim()) && isBasicType(val)) {
//            return val.toString().matches(regex);
//        }
//        return true;
//    }
//
//    /**
//     * 获取类的所有属性列表，包含所继承的
//     *
//     * @param cls
//     * @return
//     */
//    public static List<Field> getFields(Class cls) {
//        List<Field> fields = new ArrayList<>();
//        if (cls != null) {
//            Class clsTemp = cls.getSuperclass();
//            if (clsTemp != null) {
//                fields.addAll(getFields(clsTemp));
//            }
//            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
//        }
//        return fields;
//    }
//
//
//    /**
//     * 判断是否是基本类型或基本包装类
//     *
//     * @param o
//     * @return
//     */
//    public static boolean isBasicClass(Object o) {
//        try {
//            return ((Class) o.getClass().getField("TYPE").get(null)).isPrimitive();
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * 打印对象属性列表
//     */
//    public static void printParameters(Class cls) {
//        if (cls == null) {
//            System.out.println("类型为null");
//        }
//
//        for (Field field : getFields(cls)) {
//            CheckValue checkValue = field.getAnnotation(CheckValue.class);
//            String msg = checkValue == null || checkValue.value() == null || "".equalsIgnoreCase(checkValue.value()) ? "" : "  // " + checkValue.value();
//            System.out.println("\"" + cls.getSimpleName() + "." + field.getName() + "\", " + msg);
//        }
//    }
//
//
//    public static void main(String[] args) {
//        Demo demo = new Demo();
//        demo.setItem(new DemoItem());
//
//        demo.setDoub("-25.67");
//        demo.setStrTime("2022-08-04 18:42:35");
//        List<CheckResult> msg = CheckValueUtil.checkBeanFieldIsNotNull(demo, "S");
//
//        System.out.println(msg);
//
//    }
//
//    static class Demo {
//        @CheckValue(value = "测试字符串", flag = {"P"})
//        private String str;
//        @CheckValue(value = "测试日期", regexp = "^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})$", flag = {"P"})
//        private String strTime;
//        @CheckValue("测试数字")
//        private Integer inte;
//        @CheckValue(value = "测试小数", max = 50, min = -25.66)
//        private String doub;
//        @CheckValue(value = "测试子对象", flag = {"P", "E"}, isChild = true)
//        private DemoItem item;
//
//        public String getStr() {
//            return str;
//        }
//
//        public void setStr(String str) {
//            this.str = str;
//        }
//
//        public Integer getInte() {
//            return inte;
//        }
//
//        public void setInte(Integer inte) {
//            this.inte = inte;
//        }
//
//        public DemoItem getItem() {
//            return item;
//        }
//
//        public void setItem(DemoItem item) {
//            this.item = item;
//        }
//
//        public String getDoub() {
//            return doub;
//        }
//
//        public void setDoub(String doub) {
//            this.doub = doub;
//        }
//
//        public String getStrTime() {
//            return strTime;
//        }
//
//        public void setStrTime(String strTime) {
//            this.strTime = strTime;
//        }
//    }
//
//    static class DemoItem {
//        @CheckValue("子对象字符")
//        private String strItem;
//        @CheckValue(value = "子对象数字", flag = {"P", "E"}, isOptional = true)
//        private Integer inteItem;
//
//        public String getStrItem() {
//            return strItem;
//        }
//
//        public void setStrItem(String strItem) {
//            this.strItem = strItem;
//        }
//
//        public Integer getInteItem() {
//            return inteItem;
//        }
//
//        public void setInteItem(Integer inteItem) {
//            this.inteItem = inteItem;
//        }
//    }
//
//}
