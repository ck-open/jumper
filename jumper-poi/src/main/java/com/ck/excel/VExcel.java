package com.ck.excel;

import com.ck.excel.enums.VExcelWorkbookType;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName VExcel
 * @Description Excel 操作工具入口
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 * <p>
 * 基于apache poi构建，试用前需引入以下依赖
 * <!--操作Excel依赖jar包 -->
 * <dependency>
 * <groupId>org.apache.poi</groupId>
 * <artifactId>poi-excelant</artifactId>
 * <version>4.1.0</version>
 * </dependency>
 * <dependency>
 * <groupId>org.apache.poi</groupId>
 * <artifactId>poi</artifactId>
 * <!--            <version>3.17</version>-->
 * <version>4.1.0</version>
 * </dependency>
 **/
public class VExcel {

    /**
     * 构建VExcel 对象
     *
     * @param sheetDataList
     * @return
     */
    public static VExcel getInstance(List<?>... sheetDataList) {
        return new VExcel(sheetDataList);
    }


    /**
     * 需要创建的文件类型和  默认xlsx格式文件
     */
    private VExcelWorkbookType workbookType = VExcelWorkbookType.XLS_X;

    /**
     * 字符集 默认UTF-8
     */
    private Charset charsetCSV = Charset.forName("GBK");

    /**
     * 解析器
     */
    private VResolver vResolver;

    /**
     * 需要读写的Excel数据列表
     * Workbook<Sheet<Row>>
     */
    private List<List<?>> sheetDataList;


    private VExcel(List<?>... sheetDataList) {
        this.sheetDataList = new ArrayList<>();
        this.addSheets(sheetDataList);
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param workbookType Excel 文件类型
     * @param file         Excel 文件
     */
    public void read(VExcelWorkbookType workbookType, File file) {
        if (workbookType != null) this.setWorkbookType(workbookType);
        read(file);
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param file Excel 文件
     */
    public void read(File file) {
        getVResolver().read(file);
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param workbookType Excel 文件类型
     * @param inputStream  Excel 文件输入流
     */
    public void read(VExcelWorkbookType workbookType, InputStream inputStream) {
        if (workbookType != null) this.setWorkbookType(workbookType);
        read(inputStream);
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param inputStream Excel 文件输入流
     */
    public void read(InputStream inputStream) {
        getVResolver().read(inputStream);
    }

    /**
     * 解读指定的Sheet页为指定的对象列表
     *
     * @param sheetName sheet页名称
     * @param tClass    接收数据的实体对象类型
     * @param <T>
     * @return
     */
    public <T> List<T> readData(String sheetName, Class<T> tClass) {
        return getVResolver().readData(sheetName, tClass);
    }


    /**
     * 解读指定的Sheet页为指定的对象列表
     *
     * @param sheetNumber sheet页序号,物理需要，使用此方法时需注意工作簿中的sheet页准确的序号
     * @param tClass      接收数据的实体对象类型
     * @param <T>
     * @return
     */
    public <T> List<T> readData(int sheetNumber, Class<T> tClass) {
        return getVResolver().readData(sheetNumber, tClass);
    }

    /**
     * 解读指定的Sheet页为指定的对象列表
     *
     * @param tClass 接收数据的实体对象类型
     * @param <T>
     * @return
     */
    public <T> List<List<T>> readData(Class<T> tClass) {
        return getVResolver().readData(tClass);
    }

    /**
     * 文件写出
     *
     * @param file
     */
    public void write(File file) {
        getVResolver().write(file);
    }

    /**
     * 文件写出
     *
     * @param outputStream
     */
    public void write(OutputStream outputStream) {
        getVResolver().write(outputStream);
    }


    /**
     * 批量添加sheet数据
     *
     * @param sheetDataList
     * @return
     */
    public VExcel addSheets(List<?>... sheetDataList) {
        if (sheetDataList != null) for (List<?> sheet : sheetDataList) this.addSheet(sheet);
        return this;
    }

    /**
     * 添加sheet数据
     *
     * @param sheet
     * @return
     */
    public VExcel addSheet(List<?> sheet) {
        if (sheet != null) {
            if (this.sheetDataList == null) this.sheetDataList = new ArrayList<>();
            this.sheetDataList.add(sheet);
        }
        return this;
    }

    public VExcelWorkbookType getWorkbookType() {
        return workbookType;
    }

    public VExcel setWorkbookType(VExcelWorkbookType workbookType) {
        this.workbookType = workbookType;
        return this;
    }

    public List<List<?>> getSheetDataList() {
        return sheetDataList;
    }

    public VResolver getVResolver() {
        if (this.vResolver == null) this.vResolver = VResolver.getInstance(this);
        return this.vResolver;
    }

    public Charset getCharsetCSV() {
        return charsetCSV;
    }

    public void setCharsetCSV(Charset charsetCSV) {
        this.charsetCSV = charsetCSV;
    }
}
