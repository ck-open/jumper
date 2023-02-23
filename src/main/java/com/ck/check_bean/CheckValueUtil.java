package com.ck.check_bean;

import com.ck.check_bean.annotation.CheckValue;
import lombok.val;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * 实体对象属性非空校验工具
 *
 * @author cyk
 * @since 2021-06-06
 */
public final class CheckValueUtil {

    /**
     * 递归非空与正则校验
     *
     * @param o 校验的对象
     * @return
     */
    public static List<CheckResult> checkBeanFieldIsNotNull(Object o) {
        return checkBeanFieldIsNotNull(o, null);
    }

    /**
     * 递归非空与正则校验
     *
     * @param o    校验的对象
     * @param flag 自定义是否校验标签
     * @return
     */
    public static List<CheckResult> checkBeanFieldIsNotNull(Object o, String flag) {
        Objects.requireNonNull(o, "检查的目标对象不能为空");

        List<CheckResult> result = new ArrayList<>();
        Class cla = o.getClass();
        for (Field field : getFields(cla)) {
            CheckValue checkValue = field.getAnnotation(CheckValue.class);
            if (checkValue == null) continue;

            String key = cla.getSimpleName() + "." + field.getName();
            Object val = getVal(field, o, key);

            CheckResult checkResult = new CheckResult().setCla(cla).setField(field).setValue(val);

            CheckItem checkItem = CheckItem.build(checkValue);
            checkItem.setValue(Optional.ofNullable(checkItem.getValue()).orElse(key));

            val = checkValueIsNotNull(val, checkItem, flag, checkResult);

            if (!checkResult.isSucceed())
                result.add(checkResult);

            if (isBasicType(val)) {
                setVal(field, o, val, key);
            } else if (val != null && checkValue.isChild()) {
                // 复杂类型进行递归
                if (Collection.class.isAssignableFrom(val.getClass())) {
                    for (Object item : (Collection) val) {
                        result.addAll(checkBeanFieldIsNotNull(item, flag));
                    }
                } else if (val.getClass().isArray()) {   // 参数是数组
                    for (Object item : (Object[]) val) {
                        result.addAll(checkBeanFieldIsNotNull(item, flag));
                    }
                } else if (Map.class.isAssignableFrom(val.getClass())) {
                    for (Object item : ((Map) val).values()) {
                        result.addAll(checkBeanFieldIsNotNull(item, flag));
                    }
                } else {
                    result.addAll(checkBeanFieldIsNotNull(val, flag));
                }
            }
        }
        return result;
    }

    /**
     * 递归非空与正则校验
     *
     * @param o    校验的对象
     * @param flag 自定义是否校验标签
     * @return
     */
    public static List<CheckResult> checkBeanFieldIsNotNull(Map<String, Object> o, String flag, CheckItem checkItem) {
        Objects.requireNonNull(o, "检查的目标对象不能为空");
        Objects.requireNonNull(checkItem, "检查的条件对象不能为空");
        Objects.requireNonNull(checkItem.getCheckItemChild(), "检查的条件对象不能为空");

        List<CheckResult> result = new ArrayList<>();

        checkItem.getCheckItemChild().forEach((key, checkItemTemp) -> {
            if (checkItem.getCheckItemChild().containsKey(key)) {
                Object val = o.get(key);
                CheckResult checkResult = new CheckResult().setValue(val);
                val = checkValueIsNotNull(val, checkItemTemp, flag, checkResult);

                if (!checkResult.isSucceed())
                    result.add(checkResult);

                if (isBasicType(val)) {
                    o.put(key, val);
                } else if (val != null && checkItemTemp.isChild()) {
                    // 复杂类型进行递归
                    if (Collection.class.isAssignableFrom(val.getClass()) || val.getClass().isArray()) {
                        Collection valTemp = null;
                        if (val.getClass().isArray()) {
                            valTemp = Collections.singletonList(val);
                        } else {
                            assert val instanceof Collection;
                            valTemp = (Collection) val;
                        }

                        for (Object item : valTemp) {
                            if (!isBasicType(item) && Map.class.isAssignableFrom(item.getClass())) {
                                result.addAll(checkBeanFieldIsNotNull((Map<String, Object>) item, flag, checkItemTemp));
                            }
                        }

                    } else if (Map.class.isAssignableFrom(val.getClass())) {
                        result.addAll(checkBeanFieldIsNotNull((Map<String, Object>) val, flag, checkItemTemp));
                    } else {
                        result.addAll(checkBeanFieldIsNotNull(val, flag));
                    }
                }
            }
        });

        return result;
    }


    /**
     * 非空与正则校验
     *
     * @param val         校验的值
     * @param checkItem   配置的校验规则
     * @param flag        自定义是否校验标签
     * @param checkResult 校验结果信息对象
     * @return 返回校验后的值（配置有默认值则返回默认值）
     */
    private static Object checkValueIsNotNull(Object val, CheckItem checkItem, String flag, CheckResult checkResult) {
        if (checkItem != null) {
            Objects.requireNonNull(checkItem.getValue(), "非空规则校验参数名称必须指定不能为空");
            Objects.requireNonNull(checkResult, "校验结果收集器不能为空");
            if (val != null && "".equals(val.toString().trim())) {
                val = null;
            }

            checkResult.setRegexp(checkItem.getRegexp());
            if (val == null) {
                if (checkItem.getDefaultValue() != null && !"".equalsIgnoreCase(checkItem.getDefaultValue())) {
                    val = checkItem.getDefaultValue();
                } else if (!checkItem.isOptional()  //  必传属性 或 flag标记中包含指定的标记则校验非空 否则跳过
                        || Arrays.asList(checkItem.getFlag()).contains(flag)) {
                    checkResult.setMessage(checkItem.getValue() + "为空");
                }
            } else {
                if (!checkRegex(val, checkItem.getRegexp())) {
                    checkResult.setRegexp(checkItem.getRegexp()).setMessage(checkItem.getValue() + "规则不符");
                } else {
                    if (checkItem.getMax() != null && !checkMax(checkItem.getMax(), val, checkItem.getValue())) {
                        checkResult.setMax(checkItem.getMax()).setMessage(String.format("[%s]大于限定值[%s]", checkItem.getValue(), checkItem.getMax()));
                    }
                    if (checkItem.getMin() != null && !checkMin(checkItem.getMin(), val, checkItem.getValue())) {
                        checkResult.setMin(checkItem.getMin()).setMessage(String.format("[%s]小于限定值[%s]", checkItem.getValue(), checkItem.getMin()));
                    }
                }
            }
        }
        if (checkResult.getMessage() == null)
            checkResult.setSucceed(true);
        return val;
    }


    /**
     * 获取属性值
     *
     * @param field
     * @param o
     * @return
     */
    private static Object getVal(Field field, Object o, String fieldName) {
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("获取属性值异常，属性:%s ", fieldName));
        }
    }

    /**
     * 为属性赋默认值
     *
     * @param field
     * @param o
     * @param defaultValue
     * @param fieldName
     */
    private static void setVal(Field field, Object o, Object defaultValue, String fieldName) {
        try {
            if (field.getType().getSimpleName().equals(String.class.getSimpleName())) {
                field.set(o, defaultValue);
            } else if (field.getType().getSimpleName().equals(Integer.class.getSimpleName())) {
                field.set(o, Integer.valueOf(String.valueOf(defaultValue)));
            } else if (field.getType().getSimpleName().equals(Double.class.getSimpleName())) {
                field.set(o, Double.valueOf(String.valueOf(defaultValue)));
            } else if (field.getType().getSimpleName().equals(BigDecimal.class.getSimpleName())) {
                field.set(o, new BigDecimal(String.valueOf(defaultValue)));
            } else if (field.getType().getSimpleName().equals(BigInteger.class.getSimpleName())) {
                field.set(o, new BigInteger(String.valueOf(defaultValue)));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("为属性[%s]赋默认值[%s]异常, 需要的类型为[%s]！", fieldName, defaultValue, field.getType().getSimpleName()));
        }
    }

    /**
     * 最大值校验
     *
     * @param max
     * @param val
     * @return
     */
    private static boolean checkMax(double max, Object val, String fieldName) {
        if (RegExUtil.checkNumber(String.valueOf(val))) {
            if (max < Double.parseDouble(String.valueOf(val))) {
                return false;
            }
        } else {
            throw new RuntimeException(String.format("最大值只能用于数字类型校验,参数：%s  值：%s", fieldName, String.valueOf(val)));
        }
        return true;
    }

    /**
     * 最小值校验
     *
     * @param min
     * @param val
     * @return
     */
    private static boolean checkMin(double min, Object val, String fieldName) {
        if (RegExUtil.checkNumber(String.valueOf(val))) {
            if (min > Double.parseDouble(String.valueOf(val))) {
                return false;
            }
        } else {
            throw new RuntimeException(String.format("最小值只能用于数字类型校验,参数：%s  值：%s", fieldName, String.valueOf(val)));
        }
        return true;
    }

    /**
     * 判断对象是否基本类型或常见类型
     */
    public static boolean isBasicType(Object o) {
        if (o != null) {
            // 基本类型不递归
            if (isBasicClass(o)) {
                return true;
            }

            // 以下类型不进行递归
            List<String> types = Arrays.asList("String", "Date", "Calendar", "LocalTime", "BigDecimal", "BigInteger");
            if (types.contains(o.getClass().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按表达式检查数据对象是否合法
     * <p>值判断基本类型，非基本类型直接返回true</p>
     *
     * @param val   值对象
     * @param regex 正则表达式
     * @return
     */
    public static boolean checkRegex(Object val, String regex) {
        if (regex != null && !"".equals(regex.trim()) && isBasicType(val)) {
            return val.toString().matches(regex);
        }
        return true;
    }

    /**
     * 获取类的所有属性列表，包含所继承的
     *
     * @param cls
     * @return
     */
    public static List<Field> getFields(Class cls) {
        List<Field> fields = new ArrayList<>();
        if (cls != null) {
            Class clsTemp = cls.getSuperclass();
            if (clsTemp != null) {
                fields.addAll(getFields(clsTemp));
            }
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        }
        return fields;
    }


    /**
     * 判断是否是基本类型或基本包装类
     *
     * @param o
     * @return
     */
    public static boolean isBasicClass(Object o) {
        try {
            return ((Class) o.getClass().getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
