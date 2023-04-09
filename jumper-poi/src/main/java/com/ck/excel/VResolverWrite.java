package com.ck.excel;

import com.ck.excel.annotation.VExcelCell;
import com.ck.excel.annotation.VExcelTable;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName VResolverWrite
 * @Description Excel解析器 写操作
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public final class VResolverWrite {
    private static Logger log = Logger.getLogger(VResolverWrite.class.getName());

    /**
     * 将对象转换Sheet数据添加到Workbook
     *
     * @param workbook      工作簿对象
     * @param sheetDataList 工作表对象列表
     * @return
     */
    public static Workbook createSheets(Workbook workbook, List<?>... sheetDataList) {
        return createSheets(workbook, Arrays.asList(sheetDataList));
    }

    /**
     * 将对象转换Sheet数据添加到Workbook
     *
     * @param workbook      工作簿对象
     * @param sheetDataList 工作表对象列表
     * @return
     */
    public static Workbook createSheets(Workbook workbook, List<List<?>> sheetDataList) {

        if (workbook == null || sheetDataList == null) {
            throw new NullPointerException("Workbook 与 Data 参数不能为空");
        }

        try {
            for (List<?> sheetData : sheetDataList) {
                if (sheetData.size() < 1) continue;
                // 读取类注解ETable
                VExcelTable vExcelTable = sheetData.get(0).getClass().getDeclaredAnnotation(VExcelTable.class);
                if (vExcelTable == null) continue;
                // 创建Sheet 页  如果名称已存在则向后增加序列
                String sheetName = "".equals(vExcelTable.sheetName().trim()) ? "VExcelData" : vExcelTable.sheetName();
                if (workbook.getSheet(sheetName) != null) {
                    int index = 0;
                    do {
                        sheetName += ++index;
                    } while (workbook.getSheet(sheetName) != null);
                }

                Sheet sheet = workbook.createSheet(sheetName);

                // 记录包含ECell 注解的Field 名称 及对应的单元格样式对象CellStyle
                Map<String, CellStyle> fieldStyle = VResolverStyle.parseCellStyleByVExcelCell(sheet, sheetData.get(0).getClass());
                if (fieldStyle.size() < 1) continue; // 对象参数无VExcelCell注解则跳过本sheet页数据写出

                // 数据第一行 行号小于指定的数据所在行 则按照指定的行号创建
                int firstRowNumber = vExcelTable.titleRowNumber() == -1 ? sheet.getLastRowNum() : vExcelTable.titleRowNumber();
                Row row = sheet.createRow(firstRowNumber);

                // 设置标题
                if (!"".equals(vExcelTable.value().trim())) {
                    mergedRegion(sheet, row.getRowNum(), row.getRowNum(), 0, fieldStyle.size() - 1);
                    CellStyle style = VResolverStyle.parseCellStyleByVExcelTable(sheet, vExcelTable);
                    for (int i = 0; i < fieldStyle.size(); i++)
                        row.createCell(i).setCellStyle(style);
                    setValByType(row.getCell(0), vExcelTable.value());

                    row = sheet.createRow(sheet.getLastRowNum() + 1);
                }

                // 设置表头
                Map<String, CellStyle> titleStyle = VResolverStyle.parseCellStyleByVExcelTitle(sheet, sheetData.get(0));
                for (Field field : sheetData.get(0).getClass().getDeclaredFields()) {
                    if (!fieldStyle.containsKey(field.getName())) continue;
                    // 读取对象 ECell 注解
                    VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);

                    // 设置单元格值
                    Cell cell = row.createCell(row.getLastCellNum() < 0 ? 0 : row.getLastCellNum());
                    setValByType(cell, "".equals(vExcelCell.value().trim()) ? field.getName() : vExcelCell.value());
                    cell.setCellStyle(titleStyle.containsKey(field.getName()) ? titleStyle.get(field.getName()) : fieldStyle.get(field.getName()));
                }

                for (Object rowData : sheetData) {
                    row = sheet.createRow(sheet.getLastRowNum() + 1);
                    // 设置行高
                    if (vExcelTable.rowHeight() != -1) {
                        sheet.getRow(row.getRowNum()).setHeightInPoints(vExcelTable.rowHeight());
                    }

                    for (Field field : rowData.getClass().getDeclaredFields()) {
                        if (!fieldStyle.containsKey(field.getName())) continue;
                        // 设置单元格值
                        Cell cell = row.createCell(row.getLastCellNum() < 0 ? 0 : row.getLastCellNum());
                        field.setAccessible(true);
                        setValByType(cell, field.get(rowData));
                        cell.setCellStyle(fieldStyle.get(field.getName()));
                    }
                }

                // 设置自适应宽度
                setAutoSizeColumn(sheet, sheetData.get(0).getClass());
            }
        } catch (IllegalAccessException e) {
            log.log(Level.WARNING, "创建Workbook及Sheet数据异常", e);
            throw new RuntimeException("创建Workbook及Sheet数据异常", e);
        }
        return workbook;
    }

    /**
     * 设置自适应宽度  需要在所有数据组装完成后调用
     *
     * @param sheet Sheet页
     * @param cla   数据实体对象
     */
    public static void setAutoSizeColumn(Sheet sheet, Class<?> cla) {
        if (sheet == null || cla == null || sheet instanceof HSSFSheet) return;
        SXSSFSheet sxssfSheet = (SXSSFSheet) sheet;
        sxssfSheet.trackAllColumnsForAutoSizing();
        int index = 0;
        for (Field field : cla.getDeclaredFields()) {
            VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
            if (vExcelCell != null) {
                if (vExcelCell.autoSizeColumn()) {
                    sxssfSheet.autoSizeColumn(index);
                }
                index++;
            }
        }
    }


    /**
     * 合并单元格
     * 必须在设置单元格样式之后再进行单元格合并
     *
     * @param sheet         Sheet页
     * @param rowNum        合并范围开始行
     * @param rowNumLast    合并范围结束行
     * @param columnNum     合并范围开始列
     * @param columnNumLast 合并范围结束列
     */
    public static void mergedRegion(Sheet sheet, int rowNum, int rowNumLast, int columnNum, int columnNumLast) {
        try {
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNumLast, columnNum, columnNumLast));
        } catch (Exception e) {
            log.log(Level.WARNING, "单元格合并异常", e);
            throw new RuntimeException("单元格合并失败！  合并范围： " + rowNum + ":" + rowNumLast + " 至 " + columnNum + ":" + columnNumLast);
        }
    }


    /**
     * 按数据类型添加数据到单元格
     *
     * @param cell 单元格对象
     * @param val  值
     */
    public static Cell setValByType(Cell cell, Object val) {
        if (cell == null || val == null || "".equals(val))
            return cell;
        String vlaClassSimpleName = val.getClass().getSimpleName();

        switch (vlaClassSimpleName) {
            case "Integer":
            case "Number":
                cell.setCellValue(((Number) val).doubleValue());
                break;
            case "Date":
                cell.setCellValue((Date) val);
                break;
            case "Calendar":
                cell.setCellValue(((Calendar) val).getTime());
                break;
            case "BigDecimal":
                cell.setCellValue(((BigDecimal) val).doubleValue());
                break;
            case "BigInteger":
                cell.setCellValue(((BigInteger) val).intValue());
                break;
            default:
                cell.setCellValue(val.toString());
        }

        return cell;
    }

}
