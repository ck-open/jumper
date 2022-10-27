package com.ck.check_bean;


import java.util.Arrays;
import java.util.List;

/**
 * 正则验证工具类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class RegExUtil {
    /**
     * 手机号码匹配
     */
    public static final String MOBILE_REG = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
    /**
     * 座机号码匹配
     */
    public static final String PHONE_REG = "^\\d{3}-\\d{8}|\\d{4}-\\d{7}$";
    /**
     * 汉字匹配
     */
    public static final String CHINESE_REG = "^[\\u4e00-\\u9fa5]{0,}$";
    /**
     * 英文和数字匹配(不区分大小写)
     */
    public static final String LETTER_NUMBER_REG = "^[A-Za-z0-9]+$";

    /**
     * 邮箱email
     */
    public static final String EMAIL_REG = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    /**
     * 日期规则
     */
    public static final String DATE_YMD_REG = "^(\\d{4}-\\d{2}-\\d{2})$";
    /**
     * 时间规则
     */
    public static final String DATE_YMDHMS_REG = "^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})$";
    /**
     * 银行卡卡号位数
     */
    public final static String BANK_CARD_NUMBER = "^\\d{16}|\\d{19}$";
    /**
     * 身份证号码位数限制
     */
    public final static String ID_CARD = "^\\d{15}|(\\d{17}[0-9,x,X])$";

    /**
     * 身份证号码 15位格式
     * <p>
     * xxxxxx    yy MM dd   75 0     十五位
     * xxxxxx yyyy MM dd 375 0     十八位
     * 十五位：^[1-9]\d{5}\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{2}[0-9Xx]$
     * 十八位：^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$
     * 注释：编码规则顺序从左至右依次为6位数字地址码，6位数字出生年份后两位及日期，3位数字顺序码。
     * 地区：[1-9]\d{5}
     * 年的前两位：(18|19|([23]\d))            1800-3999
     * 年的后两位：\d{2}
     * 月份：((0[1-9])|(10|11|12))
     * 天数：(([0-2][1-9])|10|20|30|31)          闰年不能禁止29+
     * 三位顺序码：\d{3}
     * 两位顺序码：\d{2}
     * 校验码：[0-9Xx]
     * </p>
     */
    public final static String ID_CARD_FORMAT = "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}[0-9Xx]$)";

    /**
     * 纯字母
     */
    public static final String LETTER_REG = "^[a-zA-Z]{1,}$";
    /**
     * 数字
     */
    public static final String NUMBER_REG = "^[+-]?((\\d{1,14})|(0{1}))(\\.\\d{1,})?$";
    /**
     * 数字  小数点前14后2位
     */
    public static final String NUMBER_FLOAT_14_2_REG = "^((\\d{1,14})|(0{1}))(\\.\\d{0,2})?$";
    /**
     * 数字  整数
     */
    public static final String NUMBER_FLOAT_INTEGER_REG = "^(0|[1-9][0-9]*|-[1-9][0-9]*)$";
    /**
     * 数字  整数不含0
     */
    public static final String NUMBER_FLOAT_INTEGER_NOT_0_REG = "^([1-9][0-9]*|-[1-9][0-9]*)$";
    /**
     * 数字  正数
     */
    public static final String NUMBER_FLOAT_POSITIVE_REG = "^([0-9]*)$";
    /**
     * 数字  正数不含0
     */
    public static final String NUMBER_FLOAT_POSITIVE_NOT_0_REG = "^([1-9][0-9]*)$";
    /**
     * 数字  负数
     */
    public static final String NUMBER_FLOAT_MINUS_REG = "^0|(-[0-9]*)(\\.\\d{1,})?$";
    /**
     * 数字  负数不含0
     */
    public static final String NUMBER_FLOAT_MINUS_NOT_0_REG = "^(-[0-9]*)(\\.\\d{1,})?$";

    /**
     * 电话座机格式验证
     *
     * @return
     */
    public static boolean checkPhone(String str) {
        if (str != null)
            return str.matches(PHONE_REG);
        return false;
    }

    /**
     * 移动手机号验证
     *
     * @return
     */
    public static boolean checkMobile(String str) {
        if (str != null)
            return str.matches(MOBILE_REG);
        return false;
    }

    /**
     * 汉字匹配验证
     *
     * @return
     */
    public static boolean checkChinese(String str) {
        if (str != null)
            return str.matches(CHINESE_REG);
        return false;
    }

    /**
     * 英文和数字匹配(不区分大小写)
     *
     * @param str
     * @return
     */
    public static boolean checkLetterNumber(String str) {
        if (str != null)
            return str.matches(LETTER_NUMBER_REG);
        return false;
    }


    /**
     * 纯字母
     *
     * @param str
     * @return
     */
    public static boolean checkOnlyLetter(String str) {
        if (str != null)
            return str.matches(LETTER_REG);
        return false;
    }

    /**
     * 邮箱email
     *
     * @param str
     * @return
     */
    public static boolean checkEmail(String str) {
        if (str != null)
            return str.matches(EMAIL_REG);
        return false;
    }

    /**
     * 日期规则
     *
     * @param str
     * @return
     */
    public static boolean checkDate_YMd(String str) {
        if (str != null)
            return str.matches(DATE_YMD_REG);
        return false;
    }

    /**
     * 时间规则
     *
     * @param str
     * @return
     */
    public static boolean checkDate_YMdhms(String str) {
        if (str != null)
            return str.matches(DATE_YMDHMS_REG);
        return false;
    }

    /**
     * 银行卡卡号位数
     *
     * @param str
     * @return
     */
    public static boolean checkBankCardNumber(String str) {
        if (str != null)
            return str.matches(BANK_CARD_NUMBER);
        return false;
    }

    /**
     * 身份证号码位数限制
     *
     * @param str
     * @return
     */
    public static boolean checkIdCard(String str) {
        if (str != null)
            return str.matches(ID_CARD_FORMAT);
        return false;
    }


    /**
     * URL 地址/* 通配符规则匹配
     *
     * @param reg 通配符地址
     * @param url 比较地址
     * @return
     */
    public static boolean checkMatchingUrl(String reg, String url) {

        if (reg == null || "".equalsIgnoreCase(reg) || url == null || "".equalsIgnoreCase(url)) {
            return false;
        }

        if (reg.equalsIgnoreCase(url)) {
            return true;
        }

        // 如果有匹配符** 则转换
        if (reg.indexOf("**") > 0) {
            reg = reg.replaceAll("\\*\\*", "*");
        }
        if (reg.indexOf("***") > 0) {
            reg = reg.replaceAll("\\*\\*\\*", "*");
        }


        // 如果是根级通配符则直接返回
        if ("/*".equals(reg))
            return true;

        //按 * 切割字符串
        String[] reg_split = reg.split("/");
        String[] url_split = url.split("/");


        for (int i = 0; i < url_split.length; i++) {

            if (reg_split.length <= i) {
                return false;
            }

            // 不是是通配符 且 字符串不能完全匹配 不区分大小写
            if (!"*".equalsIgnoreCase(reg_split[i]) && !reg_split[i].equalsIgnoreCase(url_split[i])) {
                return false;
            }

            // 遍历的是条件的最后一个
            if (i == reg_split.length - 1 && reg_split.length != url_split.length) {
                // 匹配条件是通配符
                if (reg_split[i].equalsIgnoreCase("*")) {
                    break;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 字符串正则 数字 校验
     * <p>
     * 包含正负数和小数
     * </p>
     *
     * @param val
     * @return
     */
    public static boolean checkNumber(String val) {
        if (val != null && val.matches(NUMBER_REG)) {
            return true;
        }
        return false;
    }

    /**
     * 字符串正则校验
     * <p>
     * 正数
     * 小数点前 14 位， 后 2位
     * </p>
     *
     * @param val
     * @return
     */
    public static boolean checkNumberFloat_14_2(String val) {
        if (val != null && val.matches(NUMBER_FLOAT_14_2_REG)) {
            return true;
        }
        return false;
    }

    /**
     * 生成包含指定列表的正则格式
     *
     * @param in
     * @return
     */
    public static String getInRegEx(List<String> in) {
        if (in == null || in.isEmpty()) return null;
        StringBuffer reg = new StringBuffer();
        in.forEach(i -> reg.append("|").append(i));
        return String.format("^%s$", reg.toString().substring(1));
    }

    /**
     * 判断包含
     *
     * @param val
     * @param in
     * @return 不包含返回false
     */
    public static boolean checkIn(Object val, List in) {
        if (in == null || in.isEmpty() || val == null) return false;
        return in.contains(val);
    }


    public static void main(String[] args) {

        System.out.println("13043522021551X".matches(ID_CARD_FORMAT));
        System.out.println("121354.42".matches(NUMBER_REG));

        System.out.println(getInRegEx(Arrays.asList("05", "08", "01")));
        System.out.println("".matches("^05|08|01$"));
        System.out.println("0".matches(NUMBER_FLOAT_POSITIVE_REG));

    }

}
