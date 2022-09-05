package com.ck.v_excel.annotation;

import com.ck.v_excel.enums.VExcelCellFormatEnum;
import com.ck.v_excel.enums.VExcelStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.lang.annotation.*;

/**
 * @author Cyk
 * @description Excel表格标记
 * @since 2022/9/4 18:30
 **/
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface VExcelTable {
    /**
     * 表头标题  默认空  有值则占用表格第一行展示
     *
     * @return
     */
    String value() default "";

    /**
     * Sheet页名称 默认不设置
     *
     * @return
     */
    String sheetName() default "";

    /**
     * 行高  默认不设置
     */
    int rowHeight() default -1;

    /**
     * 数据（标题）所在行  默认不设置  0表示表格第一行
     */
    int titleRowNumber() default -1;


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
    HorizontalAlignment alignment() default HorizontalAlignment.CENTER;

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
     *
     * @return
     */
    String fontName() default "宋体";

    /**
     * 字体大小  默认不设置
     */
    short fontSize() default -1;

    /**
     * 字体加粗 默认不加粗
     *
     * @return
     */
    boolean fontBold() default true;

    /**
     * 字体颜色  默认 不设置
     */
    VExcelStyle.ColorEnum fontColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 字体颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] fontColorCustom() default {};

    /**
     * 单元格边框样式  默认实线
     *
     * @return
     */
    VExcelStyle.BorderEnum border() default VExcelStyle.BorderEnum.THIN;

    /**
     * 单元格顶部边框样式  默认无
     *
     * @return
     */
    VExcelStyle.BorderEnum borderTop() default VExcelStyle.BorderEnum.NONE;

    /**
     * 单元格右侧边框样式  默认无
     *
     * @return
     */
    VExcelStyle.BorderEnum borderRight() default VExcelStyle.BorderEnum.NONE;

    /**
     * 单元格底部边框样式  默认无
     *
     * @return
     */
    VExcelStyle.BorderEnum borderBottom() default VExcelStyle.BorderEnum.NONE;

    /**
     * 单元格左侧边框样式  默认无
     *
     * @return
     */
    VExcelStyle.BorderEnum borderLeft() default VExcelStyle.BorderEnum.NONE;

    /**
     * 单元格边框颜色  默认黑色
     *
     * @return
     */
    VExcelStyle.ColorEnum borderColor() default VExcelStyle.ColorEnum.BLACK;

    /**
     * 边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] borderColorCustom() default {};

    /**
     * 单元格顶部边框颜色  默认不设置
     *
     * @return
     */
    VExcelStyle.ColorEnum borderTopColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 单元格顶部边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] borderTopColorCustom() default {};

    /**
     * 单元格右侧边框颜色  默认不设置
     *
     * @return
     */
    VExcelStyle.ColorEnum borderRightColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 单元格右侧边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] borderRightColorCustom() default {};

    /**
     * 单元格底部边框颜色  默认不设置
     *
     * @return
     */
    VExcelStyle.ColorEnum borderBottomColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 单元格底部边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] borderBottomColorCustom() default {};

    /**
     * 单元格左侧边框颜色  默认不设置
     *
     * @return
     */
    VExcelStyle.ColorEnum borderLeftColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 单元格左侧边框颜色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] borderLeftColorCustom() default {};

    /**
     * 单元格背景填充样式
     *
     * @return
     */
    FillPatternType fillPatternType() default FillPatternType.NO_FILL;

    /**
     * 单元格背景填充色  默认不设置
     *
     * @return
     */
    VExcelStyle.ColorEnum fillForegroundColor() default VExcelStyle.ColorEnum.AUTOMATIC;

    /**
     * 单元格背景填充色自定义  默认 不设置
     * 数组依次 {R,G,B}
     *
     * @return
     */
    int[] fillForegroundColorCustom() default {};

}
