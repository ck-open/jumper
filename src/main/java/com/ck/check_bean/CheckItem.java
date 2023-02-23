package com.ck.check_bean;

import com.ck.check_bean.annotation.CheckValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Data
@Accessors(chain = true)
public class CheckItem {
    private static Logger log = Logger.getLogger(CheckItem.class.getName());

    /**
     * 注解参数构建检查条件的对象
     *
     * @param checkValue
     * @return
     */
    public static CheckItem build(CheckValue checkValue) {
        return new CheckItem()
                .setValue("".equals(checkValue.value().trim()) ? null : checkValue.value())
                .setRegexp(checkValue.regexp())
                .setDefaultValue(checkValue.defaultValue())
                .setOptional(checkValue.isOptional())
                .setFlag(checkValue.flag())
                .setChild(checkValue.isChild())
                .setMax(checkValue.max() == -999999999 ? null : checkValue.max())
                .setMin(checkValue.min() == -999999999 ? null : checkValue.min())
                .setName(checkValue.name());
    }

    public static CheckItem build(Map<String, Object> param) {
        Objects.requireNonNull(param, "构建 CheckItem 时，条件对象不能为空");
        CheckItem checkItem = new CheckItem();
        param.forEach((k, v) -> {
            if ("checkItemChild".equalsIgnoreCase(k)) return;
            try {
                Field field = CheckItem.class.getDeclaredField(k);
                field.set(checkItem, v);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.info(String.format("构建 CheckItem 时未找到名称为[%s]的条件参数", k));
            }
        });
        if (param.containsKey("checkItemChild") && param.get("checkItemChild") != null) {
            checkItem.setCheckItemChild(new HashMap<>());
            ((Map<String, Object>) param.get("checkItemChild")).forEach((ik, iv) -> checkItem.getCheckItemChild().put(ik, build((Map<String, Object>) iv)));
        }
        return checkItem;
    }

    /**
     * 检查失败提示信息
     *
     * @return
     */
    private String value;

    /**
     * 属性名称
     *
     * @return
     */
    private String name;

    /**
     * 最大值
     *
     * @return
     */
    private Double max;

    /**
     * 最小值
     *
     * @return
     */
    private Double min;

    /**
     * 为空时的默认值
     *
     * @return
     */
    private String defaultValue;

    /**
     * 参数正则检查
     *
     * @return
     */
    private String regexp;

    /**
     * 是否需要子级检查
     *
     * @return
     */
    private boolean isChild = false;

    /**
     * 是否非必传参数
     *
     * @return
     */
    private boolean isOptional = false;

    /**
     * 自定义是否校验标签列表
     * 配合 isOptional() 使用  为true 时 本列表包含指定的校验标记则进行校验非空
     *
     * @return
     */
    private String[] flag = {};

    /**
     * 配置化使用时用于Map层级验证条件用
     */
    private Map<String, CheckItem> checkItemChild;
}