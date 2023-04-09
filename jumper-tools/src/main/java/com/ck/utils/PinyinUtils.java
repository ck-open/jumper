package com.ck.utils;

import com.ck.check_bean.RegExUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.logging.Logger;

/**
 * 文字转拼音
 * 依赖于开源项目 com.belerweb pinyin4j 2.5.0
 *
 * @author cyk
 * @since 2020-01-01
 */
public class PinyinUtils {
    private static Logger log = Logger.getLogger(PinyinUtils.class.getName());

    public static void main(String[] args) {
        System.out.println(toPinyin("我不信"));
        System.out.println(toPinyin("我不信", "_", false));

    }


    /**
     * 将文字转为汉语拼音 驼峰
     *
     * @param chineseLanguage 要转成拼音的中文
     * @return
     */
    public static String toPinyin(String chineseLanguage) {
        return toPinyin(chineseLanguage, "_", true, HanyuPinyinCaseType.LOWERCASE, HanyuPinyinToneType.WITHOUT_TONE, HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 将文字转为汉语拼音
     *
     * @param chineseLanguage 要转成拼音的中文
     * @param interval        拼音间隔符
     * @param isHump          是否驼峰
     * @return
     */
    public static String toPinyin(String chineseLanguage, String interval, boolean isHump) {
        return toPinyin(chineseLanguage, interval, isHump, HanyuPinyinCaseType.LOWERCASE, HanyuPinyinToneType.WITHOUT_TONE, HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 将文字转为汉语拼音
     *
     * @param chineseLanguage 要转成拼音的中文
     * @param interval        拼音间隔符
     * @param isHump          是否驼峰
     * @param caseType        输出拼音全部大|小写
     * @param toneType        带|不声调
     * @param charType
     * @return
     */
    public static String toPinyin(String chineseLanguage, String interval, boolean isHump, HanyuPinyinCaseType caseType, HanyuPinyinToneType toneType, HanyuPinyinVCharType charType) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(caseType);
        defaultFormat.setToneType(toneType);
        defaultFormat.setVCharType(charType);
        return toPinyin(chineseLanguage, interval, isHump, defaultFormat);
    }

    /**
     * 将文字转为汉语拼音
     *
     * @param chineseLanguage 要转成拼音的中文
     * @param interval        拼音间隔符
     * @param isHump          是否驼峰
     * @param defaultFormat   转换格式
     * @return
     */
    public static String toPinyin(String chineseLanguage, String interval, boolean isHump, HanyuPinyinOutputFormat defaultFormat) {
        if (chineseLanguage == null) {
            return null;
        }

        char[] cl_chars = chineseLanguage.trim().toCharArray();

        StringBuilder pinyin = new StringBuilder();

        try {
            for (int i = 0; i < cl_chars.length; i++) {
                String pin;
                if (String.valueOf(cl_chars[i]).matches("[\u4e00-\u9fa5]+")) {// 如果字符是中文,则将中文转为汉语拼音
                    String[] p;
                    if (defaultFormat == null) {
                        p = PinyinHelper.toHanyuPinyinStringArray(cl_chars[i]);
                    } else {
                        p = PinyinHelper.toHanyuPinyinStringArray(cl_chars[i], defaultFormat);
                    }
                    pin = p[0];
                } else {// 如果字符不是中文,则不转换
                    pin = String.valueOf(cl_chars[i]);
                }

                if (i != 0) {
                    if (interval != null) {
                        pinyin.append(interval);
                    }
                }
                pinyin.append(pin);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.info(String.format("字符[%s]不能转成汉语拼音", chineseLanguage));
        }

        if (isHump) {
            return RegExUtil.toHump(pinyin.toString());
        }
        return pinyin.toString();
    }
}
