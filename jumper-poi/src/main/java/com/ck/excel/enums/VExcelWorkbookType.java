package com.ck.excel.enums;

/**
 * @ClassName WorkbookType
 * @Description 文档类型
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public enum VExcelWorkbookType {
    XLS("逗号分割",".csv",65536,256),
    XLS_X("逗号分割",".csv",1048576,16384),
    CSV("逗号分割",".csv",false,-1,-1);


    /**
     * 类型名称
     */
    private String name;
    /**
     * 类型文件后缀
     */
    private String fileSuffix;
    /**
     * 文档是否支持样式
     */
    private boolean isStyle = true;
    /**
     * 文档支持最大行数
     */
    private int RowMaxNum;
    /**
     * 文档支持最大列数
     */
    private int ColumnMaxNum;

    VExcelWorkbookType(String name, String fileSuffix, int rowMaxNum, int columnMaxNum) {
        this.name = name;
        this.fileSuffix = fileSuffix;
        RowMaxNum = rowMaxNum;
        ColumnMaxNum = columnMaxNum;
    }

    VExcelWorkbookType(String name, String fileSuffix, boolean isStyle, int rowMaxNum, int columnMaxNum) {
        this.name = name;
        this.fileSuffix = fileSuffix;
        this.isStyle = isStyle;
        RowMaxNum = rowMaxNum;
        ColumnMaxNum = columnMaxNum;
    }
}
