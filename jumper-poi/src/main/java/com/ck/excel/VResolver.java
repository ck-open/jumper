package com.ck.excel;

import com.ck.excel.enums.VExcelWorkbookType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName VResolver
 * @Description 表格类解析器
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public class VResolver {
    private Logger log = Logger.getLogger(VResolver.class.getName());

    /**
     * 创建解析器对象 VResolver
     *
     * @param vExcel
     * @return
     */
    public static VResolver getInstance(VExcel vExcel) {
        return new VResolver(vExcel);
    }


    /**
     * 操作数据对象
     */
    private VExcel vExcel;

    /**
     * 工作簿对象
     */
    private Workbook workbook;
    /**
     * 读取的CSV文本数据列表
     * Row<Cell<String>>
     */
    private List<List<String>> csvDataTextList;

    private VResolver(VExcel vExcel) {
        if (vExcel == null) {
            throw new NullPointerException("VResolver 创建异常，VExcel 未空");
        }
        this.vExcel = vExcel;
    }

    /**
     * 根据 VExcelWorkbookType 创建 Workbook
     *
     * @return
     */
    public Workbook createWorkbook() {
        if (this.vExcel.getWorkbookType() == null) {
            throw new RuntimeException("Workbook 创建异常，未指定 WorkbookType");
        }
        if (VExcelWorkbookType.XLS_X.equals(this.vExcel.getWorkbookType())) {
//            this.workbook = new XSSFWorkbook();
            this.workbook = new SXSSFWorkbook(100); // poi 3.8 以后版本用于解决大数据量内存溢出的问题，写出时每100行flush一次清空内存
        } else if (VExcelWorkbookType.XLS.equals(this.vExcel.getWorkbookType())) {
            this.workbook = new HSSFWorkbook();
        }
        return this.workbook;
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param excelFile Excel 文件
     */
    public void read(File excelFile) {
        if (excelFile == null) {
            throw new NullPointerException("VExcel 文件读取失败,File is null");
        }
        if (!excelFile.exists()) {
            throw new RuntimeException(String.format("VExcel 文件读取失败,文件不存在, Path:%s FileName:%s"
                    , excelFile.getPath().replaceAll(excelFile.getName(), ""), excelFile.getName()));
        }
        try {
            read(new FileInputStream(excelFile));
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING, "VExcel 文件读取异常", e);
            throw new RuntimeException("VExcel 文件读取异常", e);
        }
    }

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param inputStream Excel 文件输入流
     */
    public void read(InputStream inputStream) {
        if (VExcelWorkbookType.CSV.equals(this.vExcel.getWorkbookType())) {
            this.csvDataTextList = VResolverCsv.read(inputStream, this.vExcel.getCharsetCSV());
        } else {
            this.workbook = VResolverRead.read(this.vExcel.getWorkbookType(), inputStream);
        }
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
        if (VExcelWorkbookType.CSV.equals(this.vExcel.getWorkbookType())) {
            return readData(null, tClass);
        } else {
            if (getWorkbook().getNumberOfSheets() < sheetNumber || sheetNumber < 1)
                throw new RuntimeException(String.format("VExcel 文件读取失败，文件下共%s页，读取的为%s页", getWorkbook().getNumberOfSheets(), sheetNumber));
            Sheet sheet = getWorkbook().getSheetAt(sheetNumber - 1);
            return readData(sheet.getSheetName(), tClass);
        }
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
        if (VExcelWorkbookType.CSV.equals(this.vExcel.getWorkbookType())) {
            return VResolverCsv.read(this.csvDataTextList, tClass);
        } else {
            return VResolverRead.readData(getWorkbook(), sheetName, tClass);
        }
    }

    /**
     * 解读指定的Sheet页为指定的对象列表
     *
     * @param tClass 接收数据的实体对象类型
     * @param <T>
     * @return
     */
    public <T> List<List<T>> readData(Class<T> tClass) {
        List<List<T>> result = new ArrayList<>();
        if (VExcelWorkbookType.CSV.equals(this.vExcel.getWorkbookType())) {
            result.add(VResolverCsv.read(this.csvDataTextList, tClass));
        } else {
            Iterator<Sheet> sheetIterator = getWorkbook().sheetIterator();
            while (sheetIterator.hasNext()) {
                result.add(readData(sheetIterator.next().getSheetName(), tClass));
            }
        }
        return result;
    }

    /**
     * 文件写出
     *
     * @param file
     */
    public void write(File file) {
        try {
            if (file == null) {
                throw new NullPointerException("VExcel 文件写出失败,File is null");
            }

            //创建文件
            if (file.createNewFile()) {
                // 文件创建成功
                log.info(String.format("文件创建创建成功, Path:%s FileName:%s", file.getPath().replaceAll(file.getName(), ""), file.getName()));
            }
            if (!file.exists()) {
                throw new RuntimeException(String.format("VExcel 文件写出失败,文件不存在且无法创建, Path:%s FileName:%s"
                        , file.getPath().replaceAll(file.getName(), ""), file.getName()));
            }

            FileOutputStream out = new FileOutputStream(file);
            write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "VExcel 文件写出异常", e);
            throw new RuntimeException(String.format("VExcel 文件写出异常:%s", e.getMessage()));
        }
    }

    /**
     * 文件写出
     *
     * @param outputStream
     */
    public void write(OutputStream outputStream) {
        try {
            if (VExcelWorkbookType.CSV.equals(this.vExcel.getWorkbookType())) {
                VResolverCsv.writer(this.vExcel.getSheetDataList().iterator().next(), outputStream, this.vExcel.getCharsetCSV());
            } else {
                VResolverWrite.createSheets(this.getWorkbook(), this.vExcel.getSheetDataList()).write(outputStream);
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "VExcel 文件写出异常", e);
            throw new RuntimeException("VExcel 文件写出异常:" + e.getMessage());
        }
    }

    public Workbook getWorkbook() {
        if (this.workbook == null) createWorkbook();
        return workbook;
    }
}
