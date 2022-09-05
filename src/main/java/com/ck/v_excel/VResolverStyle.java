package com.ck.v_excel;

import com.ck.v_excel.annotation.VExcelCell;
import com.ck.v_excel.annotation.VExcelTable;
import com.ck.v_excel.annotation.VExcelTitleStyle;
import com.ck.v_excel.enums.VExcelCellFormatEnum;
import com.ck.v_excel.enums.VExcelStyle;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName VResolverRead
 * @Description Excel样式解析器
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public final class VResolverStyle {
    private static Logger log = Logger.getLogger(VResolverStyle.class.getName());

    /**
     * 解析VExcelTable注解并创建单元格样式对象
     *
     * @param sheet 工作表对象
     * @param cla   类
     * @return
     */
    public static CellStyle parseCellStyleByVExcelTable(Sheet sheet, Class<?> cla) {
        if (cla == null) return null;
        return parseCellStyleByVExcelTable(sheet, cla.getDeclaredAnnotation(VExcelTable.class));
    }

    /**
     * 解析VExcelTable注解并创建单元格样式对象
     *
     * @param sheet       工作表对象
     * @param vExcelTable 注解
     * @return
     */
    public static CellStyle parseCellStyleByVExcelTable(Sheet sheet, VExcelTable vExcelTable) {
        if (vExcelTable == null || sheet == null) {
            return null;
        }
        CellStyle syyle = sheet.getWorkbook().createCellStyle();

        // 边框样式
        setBorder(syyle, vExcelTable.border(), vExcelTable.borderTop(), vExcelTable.borderRight(), vExcelTable.borderBottom(), vExcelTable.borderLeft());

        // 边框颜色
        setBorderColor(syyle, vExcelTable.borderColor(), vExcelTable.borderTopColor(), vExcelTable.borderRightColor(), vExcelTable.borderBottomColor(), vExcelTable.borderLeftColor()
                , vExcelTable.borderColorCustom(), vExcelTable.borderTopColorCustom(), vExcelTable.borderRightColorCustom(), vExcelTable.borderBottomColorCustom(), vExcelTable.borderLeftColorCustom());

        // 设置单元格填充
        setFillPatternType(syyle, vExcelTable.fillPatternType(), vExcelTable.fillForegroundColor(), vExcelTable.fillForegroundColorCustom());

        // 文本垂直水平对齐方式、自动换行、数据格式、文字旋转
        setDataFormat(sheet, syyle, vExcelTable.alignmentVertical(), vExcelTable.alignment(), vExcelTable.isWrapText(), vExcelTable.cellFormat(), vExcelTable.cellFormatCustom(), vExcelTable.rotation());

        // 设置字体
        setFont(sheet, syyle, vExcelTable.fontName(), vExcelTable.fontSize(), vExcelTable.fontBold(), vExcelTable.fontColor(), vExcelTable.fontColorCustom());
        return syyle;
    }

    /**
     * 解析VExcelTitle注解并创建单元格样式对象
     *
     * @param sheet      工作表对象
     * @param tableValue 表数据对象
     * @return 单元格样式 key：FieldName
     */
    public static Map<String, CellStyle> parseCellStyleByVExcelTitle(Sheet sheet, Object tableValue) {
        if (sheet == null || tableValue == null) {
            throw new NullPointerException("Workbook 与 TableValue 参数不能为空");
        }

        // 记录包含ECell 注解的Field 名称 及对应的单元格样式对象CellStyle
        Map<String, CellStyle> fieldStyle = new LinkedHashMap<>();
        // 表头 及 属性配置信息
        for (Field field : tableValue.getClass().getDeclaredFields()) {
            // 读取对象 ECell 注解
            VExcelTitleStyle vExcelTitleStyle = field.getDeclaredAnnotation(VExcelTitleStyle.class);
            if (vExcelTitleStyle == null) continue;

            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            fieldStyle.put(field.getName(), cellStyle);

            // 边框样式
            setBorder(cellStyle, vExcelTitleStyle.border(), vExcelTitleStyle.borderTop(), vExcelTitleStyle.borderRight(), vExcelTitleStyle.borderBottom(), vExcelTitleStyle.borderLeft());

            // 边框颜色
            setBorderColor(cellStyle, vExcelTitleStyle.borderColor(), vExcelTitleStyle.borderTopColor(), vExcelTitleStyle.borderRightColor(), vExcelTitleStyle.borderBottomColor(), vExcelTitleStyle.borderLeftColor()
                    , vExcelTitleStyle.borderColorCustom(), vExcelTitleStyle.borderTopColorCustom(), vExcelTitleStyle.borderRightColorCustom(), vExcelTitleStyle.borderBottomColorCustom(), vExcelTitleStyle.borderLeftColorCustom());

            // 设置单元格填充
            setFillPatternType(cellStyle, vExcelTitleStyle.fillPatternType(), vExcelTitleStyle.fillForegroundColor(), vExcelTitleStyle.fillForegroundColorCustom());

            // 文本垂直水平对齐方式、自动换行、数据格式、文字旋转
            setDataFormat(sheet, cellStyle, vExcelTitleStyle.alignmentVertical(), vExcelTitleStyle.alignment(), vExcelTitleStyle.isWrapText(), vExcelTitleStyle.cellFormat(), vExcelTitleStyle.cellFormatCustom(), vExcelTitleStyle.rotation());

            // 设置字体
            setFont(sheet, cellStyle, vExcelTitleStyle.fontName(), vExcelTitleStyle.fontSize(), vExcelTitleStyle.fontBold(), vExcelTitleStyle.fontColor(), vExcelTitleStyle.fontColorCustom());
        }
        return fieldStyle;
    }

    /**
     * 解析VExcelCell注解并创建单元格样式对象
     *
     * @param sheet 工作表对象
     * @param cla   表数据对象
     * @return 单元格样式 key：FieldName
     */
    public static Map<String, CellStyle> parseCellStyleByVExcelCell(Sheet sheet, Class<?> cla) {
        if (sheet == null || cla == null) {
            throw new NullPointerException("Workbook 与 TableValue 参数不能为空");
        }

        // 记录包含ECell 注解的Field 名称 及对应的单元格样式对象CellStyle
        Map<String, CellStyle> fieldStyle = new LinkedHashMap<>();
        Set<String> fieldSets = new HashSet<>();
        int index = 0;
        // 表头 及 属性配置信息
        for (Field field : cla.getDeclaredFields()) {
            // 读取对象 ECell 注解
            VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
            if (vExcelCell == null || vExcelCell.isRowNumber()) continue;

            if (fieldSets.add(field.getName())) {
                // 设置列宽
                if (vExcelCell.columnWidth() != -1) {
                    sheet.setColumnWidth(index++, vExcelCell.columnWidth());
                }
            }

            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            fieldStyle.put(field.getName(), cellStyle);

            // 边框样式
            setBorder(cellStyle, vExcelCell.border(), vExcelCell.borderTop(), vExcelCell.borderRight(), vExcelCell.borderBottom(), vExcelCell.borderLeft());

            // 边框颜色
            setBorderColor(cellStyle, vExcelCell.borderColor(), vExcelCell.borderTopColor(), vExcelCell.borderRightColor(), vExcelCell.borderBottomColor(), vExcelCell.borderLeftColor()
                    , vExcelCell.borderColorCustom(), vExcelCell.borderTopColorCustom(), vExcelCell.borderRightColorCustom(), vExcelCell.borderBottomColorCustom(), vExcelCell.borderLeftColorCustom());

            // 设置单元格填充
            setFillPatternType(cellStyle, vExcelCell.fillPatternType(), vExcelCell.fillForegroundColor(), vExcelCell.fillForegroundColorCustom());

            // 文本垂直水平对齐方式、自动换行、数据格式、文字旋转
            setDataFormat(sheet, cellStyle, vExcelCell.alignmentVertical(), vExcelCell.alignment(), vExcelCell.isWrapText(), vExcelCell.cellFormat()
                    , Date.class.getSimpleName().equals(field.getType().getSimpleName()) && "".equals(vExcelCell.cellFormatCustom().trim()) ? "YYYY-MM-DD HH:mm:ss" : vExcelCell.cellFormatCustom()
                    , vExcelCell.rotation());

            // 设置字体
            setFont(sheet, cellStyle, vExcelCell.fontName(), vExcelCell.fontSize(), vExcelCell.fontBold(), vExcelCell.fontColor(), vExcelCell.fontColorCustom());
        }
        return fieldStyle;
    }

    /**
     * 设置字体样式
     *
     * @param sheet           Sheet页
     * @param cellStyle       样式对象
     * @param fontName        字体名称
     * @param fontSize        字体大小
     * @param fontBold        加粗
     * @param fontColor       颜色
     * @param fontColorCustom 自定义颜色
     * @return
     */
    public static CellStyle setFont(Sheet sheet, CellStyle cellStyle, String fontName, short fontSize, boolean fontBold, VExcelStyle.ColorEnum fontColor, int[] fontColorCustom) {
        // 设置字体
        Font font = sheet.getWorkbook().createFont();
        if (fontName != null && !"".equals(fontName)) {
            font.setFontName(fontName);
            cellStyle.setFont(font);
        }
        if (fontSize != -1) { // 大小
            font.setFontHeightInPoints(fontSize);
            cellStyle.setFont(font);
        }
        if (fontBold) { // 加粗
            font.setBold(fontBold);
            cellStyle.setFont(font);
        }
        if (!VExcelStyle.ColorEnum.AUTOMATIC.equals(fontColor)) { // 字体颜色
            font.setColor(fontColor.getColor().getIndex());
            cellStyle.setFont(font);
        }
        if (fontColorCustom.length > 2) { // 字体颜色
            font.setColor(VExcelStyle.FromColor(fontColorCustom).getIndex());
            cellStyle.setFont(font);
        }
        return cellStyle;
    }

    /**
     * 设置 文本垂直水平对齐方式、自动换行、数据格式、文字旋转
     *
     * @param sheet        Sheet页
     * @param cellStyle    样式对象
     * @param vertical     垂直对齐方式
     * @param horizontal   水平对齐方式
     * @param wrapped      自动换行
     * @param formatEnum   数据格式
     * @param formatCustom 自定义 数据格式
     * @param textRotation 文字旋转角度
     * @return
     */
    public static CellStyle setDataFormat(Sheet sheet, CellStyle cellStyle, VerticalAlignment vertical, HorizontalAlignment horizontal
            , boolean wrapped, VExcelCellFormatEnum formatEnum, String formatCustom, short textRotation) {
        // 设置文本居中
        cellStyle.setVerticalAlignment(vertical); // 垂直
        cellStyle.setAlignment(horizontal);  // 水平

        // 设置自动换行
        cellStyle.setWrapText(wrapped);

        // 设置数据格式
        if (!VExcelCellFormatEnum.FORMAT_NONE.equals(formatEnum)) {
            cellStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat(formatEnum.getFormat()));
        }
        // 设置数据自定义格式
        if (formatCustom != null && !"".equals(formatCustom.trim())) {
            cellStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat(formatCustom));
        }

        // 设置文字旋转
        if (textRotation != -1) {
            cellStyle.setRotation(textRotation);
        }
        return cellStyle;
    }

    /**
     * 设置单元格填充样式及颜色
     *
     * @param cellStyle                 样式对象
     * @param fillPatternType           填充样式
     * @param fillForegroundColor       填充颜色
     * @param fillForegroundColorCustom 自定义填充颜色
     * @return
     */
    public static CellStyle setFillPatternType(CellStyle cellStyle, FillPatternType fillPatternType, VExcelStyle.ColorEnum fillForegroundColor, int[] fillForegroundColorCustom) {
        // 设置单元格填充
        if (!FillPatternType.NO_FILL.equals(fillPatternType)) {
            cellStyle.setFillPattern(fillPatternType);

            // 设置单元格填充色
            if (!VExcelStyle.ColorEnum.AUTOMATIC.equals(fillForegroundColor)) {
                cellStyle.setFillForegroundColor(fillForegroundColor.getColor().getIndex());
            }
            if (fillForegroundColorCustom.length > 2) {
                cellStyle.setFillForegroundColor(VExcelStyle.FromColor(fillForegroundColorCustom).getIndex());
            }
        }
        return cellStyle;
    }

    /**
     * 设置单元格样式
     *
     * @param cellStyle   样式对象
     * @param borderStyle 默认四边样式
     * @param top         顶部样式
     * @param right       右侧样式
     * @param bottom      底部样式
     * @param left        左侧样式
     * @return
     */
    public static CellStyle setBorder(CellStyle cellStyle, VExcelStyle.BorderEnum borderStyle
            , VExcelStyle.BorderEnum top, VExcelStyle.BorderEnum right, VExcelStyle.BorderEnum bottom, VExcelStyle.BorderEnum left) {
        // 边框样式
        cellStyle.setBorderTop(VExcelStyle.BorderEnum.NONE.equals(top) ? borderStyle.getBorderStyle() : top.getBorderStyle()); // 顶部边框
        cellStyle.setBorderRight(VExcelStyle.BorderEnum.NONE.equals(right) ? borderStyle.getBorderStyle() : right.getBorderStyle()); // 右边框
        cellStyle.setBorderBottom(VExcelStyle.BorderEnum.NONE.equals(bottom) ? borderStyle.getBorderStyle() : bottom.getBorderStyle()); // 底边框
        cellStyle.setBorderLeft(VExcelStyle.BorderEnum.NONE.equals(left) ? borderStyle.getBorderStyle() : left.getBorderStyle()); // 左边框
        return cellStyle;
    }

    /**
     * 设置单元格边框颜色
     *
     * @param cellStyle         单元格样式对象
     * @param borderStyle       默认颜色
     * @param top               顶部颜色
     * @param right             右侧颜色
     * @param bottom            底部颜色
     * @param left              左侧颜色
     * @param colorCustom       自定义颜色
     * @param topColorCustom    自定顶部颜色
     * @param rightColorCustom  自定义右侧颜色
     * @param bottomColorCustom 自定义底部颜色
     * @param leftColorCustom   自定义左侧颜色
     * @return
     */
    public static CellStyle setBorderColor(CellStyle cellStyle, VExcelStyle.ColorEnum borderStyle
            , VExcelStyle.ColorEnum top, VExcelStyle.ColorEnum right, VExcelStyle.ColorEnum bottom, VExcelStyle.ColorEnum left
            , int[] colorCustom, int[] topColorCustom, int[] rightColorCustom, int[] bottomColorCustom, int[] leftColorCustom) {


        // 默认颜色
        cellStyle.setTopBorderColor(VExcelStyle.ColorEnum.AUTOMATIC.equals(top) ? borderStyle.getColor().getIndex() : top.getColor().getIndex()); // 边框颜色
        cellStyle.setRightBorderColor(VExcelStyle.ColorEnum.AUTOMATIC.equals(right) ? borderStyle.getColor().getIndex() : right.getColor().getIndex()); // 边框颜色
        cellStyle.setBottomBorderColor(VExcelStyle.ColorEnum.AUTOMATIC.equals(bottom) ? borderStyle.getColor().getIndex() : bottom.getColor().getIndex()); // 边框颜色
        cellStyle.setLeftBorderColor(VExcelStyle.ColorEnum.AUTOMATIC.equals(left) ? borderStyle.getColor().getIndex() : left.getColor().getIndex()); // 边框颜色

        // 自定义边框默认颜色
        if (colorCustom.length > 2) {
            cellStyle.setTopBorderColor(VExcelStyle.FromColor(colorCustom).getIndex());
            cellStyle.setRightBorderColor(VExcelStyle.FromColor(colorCustom).getIndex());
            cellStyle.setBottomBorderColor(VExcelStyle.FromColor(colorCustom).getIndex());
            cellStyle.setLeftBorderColor(VExcelStyle.FromColor(colorCustom).getIndex());
        }
        if (topColorCustom.length > 2) {
            cellStyle.setTopBorderColor(VExcelStyle.FromColor(topColorCustom).getIndex()); // 边框颜色
        }
        if (rightColorCustom.length > 2) {
            cellStyle.setRightBorderColor(VExcelStyle.FromColor(rightColorCustom).getIndex()); // 边框颜色
        }
        if (bottomColorCustom.length > 2) {
            cellStyle.setBottomBorderColor(VExcelStyle.FromColor(bottomColorCustom).getIndex()); // 边框颜色
        }
        if (leftColorCustom.length > 2) {
            cellStyle.setLeftBorderColor(VExcelStyle.FromColor(leftColorCustom).getIndex()); // 边框颜色
        }
        return cellStyle;
    }

    /**
     * 获取样式Key
     *
     * @param val 注解对象
     * @return
     */
    private static String getStyleKey(Object val) {
        StringBuilder styleKey = new StringBuilder();
        try {
            for (Field eCellField : val.getClass().getDeclaredFields()) {
                // 非样式参数 不作为key排除
                List<String> fieldNotStyles = Arrays.asList("value", "columnWidth", "mergedRegionRow", "mergedRegionColumn", "isRowNumber");

                if (fieldNotStyles.contains(eCellField.getName()))
                    continue;
                eCellField.setAccessible(true);
                styleKey.append("-").append(resetObjectByFieldType(eCellField, eCellField.get(val)));
            }
        } catch (IllegalAccessException e) {
            log.log(Level.WARNING, "获取样式Key异常", e);
            throw new RuntimeException("获取样式Key异", e);
        }
        return styleKey.toString();
    }

    /**
     * 根据Field 类型转换对象
     *
     * @param field
     * @param val
     * @return
     */
    public static Object resetObjectByFieldType(Field field, Object val) {
        switch (field.getType().getSimpleName()) {
            case "String":
                val = val.toString();
                break;
            case "Integer":
                val = Integer.parseInt(val.toString());
                break;
            case "Double":
                val = Double.parseDouble(val.toString());
                break;
            case "Boolean":
                val = Boolean.valueOf(val.toString());
                break;
            case "BigInteger":
                val = new BigInteger(val.toString());
                break;
            case "BigDecimal":
                val = new BigDecimal(val.toString());
                break;
            default:
        }
        return val;
    }
}
