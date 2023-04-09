package com.ck.excel.enums;

/**
 * @author Cyk
 * @description Excel 边框样式
 * @since 2022/9/4 18:30
 **/
public enum VExcelCellFormatEnum {
    FORMAT_NONE("-1","常规","单元格数据格式"),
    FORMAT_NUMBER_RESOLUTION_1("0.0","保留一位小数","单元格数据格式"),
    FORMAT_NUMBER_RESOLUTION_2("0.00","保留两位小数","单元格数据格式"),
    FORMAT_NUMBER_RESOLUTION_3("0.000","保留两位小数","单元格数据格式"),
    FORMAT_NUMBER_RESOLUTION_4("0.0000","保留两位小数","单元格数据格式"),
    FORMAT_NUMBER_RESOLUTION_CAPITAL("[DbNum2][$-804]0","中文大写数字","单元格数据格式"),
    FORMAT_DATE_TIME("YYYY/MM/DD HH:mm:ss","日期时间格式","单元格数据格式"),
    FORMAT_TEXT("@","文本格式","单元格数据格式"),
    FORMAT_NUMBER_CURRENCY("¥#,##0","货币","单元格数据格式"),
    FORMAT_NUMBER_PERCENT("0.00%","百分比","单元格数据格式"),
    FORMAT_NUMBER_scientific_notation("0.00E+00","科学计数法","单元格数据格式"),
    ;


    private String format;
    private String name;
    private String message;

    VExcelCellFormatEnum(String format, String name) {
        this.format = format;
        this.name = name;
    }

    VExcelCellFormatEnum(String format, String name, String message) {
        this.format = format;
        this.name = name;
        this.message = message;
    }

    public String getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
