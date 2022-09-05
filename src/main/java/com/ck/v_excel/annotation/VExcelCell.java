package com.ck.v_excel.annotation;


import com.ck.v_excel.enums.VExcelCellFormatEnum;
import com.ck.v_excel.enums.VExcelStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.lang.annotation.*;

/**
 * @author Cyk
 * @description Excel表格列注解
 * @since 2022/9/4 18:30
 **/
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface VExcelCell {
    /**
     * 标题名称 空则取属性名
     *
     * @return
     */
    String value() default "";

    /**
     * 列宽  默认不设置
     */
    int columnWidth() default -1;

    /**
     * 单元格数据格式  默认 常规
     *
     * @return
     */
    VExcelCellFormatEnum cellFormat() default VExcelCellFormatEnum.FORMAT_NONE;

    /**
     * 单元格数据格式 自定义  默认 无
     *
     * @return
     */
    String cellFormatCustom() default "";

    /**
     * 文本旋转角度 默认不旋转
     */
    short rotation() default -1;

    /**
     * 水平对齐方式  默认靠左
     */
    HorizontalAlignment alignment() default HorizontalAlignment.LEFT;

    /**
     * 垂直对齐方式  默认居中
     */
    VerticalAlignment alignmentVertical() default VerticalAlignment.CENTER;
    /**
     * 是否文本自动换行  默认不换行
     */
    boolean isWrapText() default false;
    /**
     * 字体  默认不设置
     * @return
     */
    String fontName() default "宋体";
    /**
     * 字体大小  默认不设置
     */
    short fontSize() default -1;
    /**
     * 字体加粗 默认不加粗
     * @return
     */
    boolean fontBold() default false;
    /**
     * 字体颜色  默认 不设置
     */
    VExcelStyle.ColorEnum fontColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 字体颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] fontColorCustom() default {};

    /**
     * 单元格边框样式  默认实线
     * @return
     */
    VExcelStyle.BorderEnum border() default VExcelStyle.BorderEnum.THIN;

    /**
     * 单元格顶部边框样式  默认无
     * @return
     */
    VExcelStyle.BorderEnum borderTop() default VExcelStyle.BorderEnum.NONE;
    /**
     * 单元格右侧边框样式  默认无
     * @return
     */
    VExcelStyle.BorderEnum borderRight() default VExcelStyle.BorderEnum.NONE;
    /**
     * 单元格底部边框样式  默认无
     * @return
     */
    VExcelStyle.BorderEnum borderBottom() default VExcelStyle.BorderEnum.NONE;
    /**
     * 单元格左侧边框样式  默认无
     * @return
     */
    VExcelStyle.BorderEnum borderLeft() default VExcelStyle.BorderEnum.NONE;

    /**
     * 单元格边框颜色  默认黑色
     * @return
     */
    VExcelStyle.ColorEnum borderColor() default VExcelStyle.ColorEnum.BLACK;
    /**
     * 边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] borderColorCustom() default {};

    /**
     * 单元格顶部边框颜色  默认不设置
     * @return
     */
    VExcelStyle.ColorEnum borderTopColor() default VExcelStyle.ColorEnum.AUTOMATIC;
    /**
     * 单元格顶部边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] borderTopColorCustom() default {};
    /**
     * 单元格右侧边框颜色  默认不设置
     * @return
     */
    VExcelStyle.ColorEnum borderRightColor() default VExcelStyle.ColorEnum.AUTOMATIC;
    /**
     * 单元格右侧边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] borderRightColorCustom() default {};
    /**
     * 单元格底部边框颜色  默认不设置
     * @return
     */
    VExcelStyle.ColorEnum borderBottomColor() default VExcelStyle.ColorEnum.AUTOMATIC;
    /**
     * 单元格底部边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] borderBottomColorCustom() default {};
    /**
     * 单元格左侧边框颜色  默认不设置
     * @return
     */
    VExcelStyle.ColorEnum borderLeftColor() default VExcelStyle.ColorEnum.AUTOMATIC;
    /**
     * 单元格左侧边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] borderLeftColorCustom() default {};

    /**
     * 单元格背景填充样式
     * @return
     */
    FillPatternType fillPatternType() default FillPatternType.NO_FILL;

    /**
     * 单元格背景填充色  默认不设置
     * @return
     */
    VExcelStyle.ColorEnum fillForegroundColor() default VExcelStyle.ColorEnum.AUTOMATIC;
    /**
     * 单元格背景填充色自定义  默认 不设置
     * 数组依次 {R,G,B}
     * @return
     */
    int[] fillForegroundColorCustom() default {};

//    /**
//     * 单元格向下合并行数 默认不合并
//     * @return
//     */
//    int mergedRegionRow() default -1;
//
//    /**
//     * 单元格向右合并列数 默认不合并
//     * @return
//     */
//    int mergedRegionColumn() default -1;

    /**
     * 标记此字段是否用于记录行号的  默认未否
     * 为true时 在读写的文件中都不体现此字段，且以上所有注解配置失效
     * @return
     */
    boolean isRowNumber() default false;

    /**
     * 设置自适应列宽 默认不设置
     *
     * @return
     */
    boolean autoSizeColumn() default false;
}
