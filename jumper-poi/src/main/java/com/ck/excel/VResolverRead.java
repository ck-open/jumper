package com.ck.excel;

import com.ck.excel.annotation.VExcelCell;
import com.ck.excel.annotation.VExcelTable;
import com.ck.excel.enums.VExcelCellFormatEnum;
import com.ck.excel.enums.VExcelWorkbookType;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName VResolverRead
 * @Description Excel解析器 读操作
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public final class VResolverRead {
    private static Logger log = Logger.getLogger(VResolverRead.class.getName());

    /**
     * 解读 Excel 文件为 Workbook 对象
     *
     * @param workbookType Excel 文件类型
     * @param inputStream  Excel 文件输入流
     * @return
     */
    public static Workbook read(VExcelWorkbookType workbookType, InputStream inputStream) {
        if (inputStream == null) throw new NullPointerException("VResolverRead 解读 Workbook 工作簿失败，InputStream is null");
        if (workbookType == null)
            throw new NullPointerException("VResolverRead 解读 Workbook 工作簿失败，VExcelWorkbookType is null");
        try {
            if (VExcelWorkbookType.XLS_X.equals(workbookType)) {
                return new XSSFWorkbook(inputStream);
            } else if (VExcelWorkbookType.XLS.equals(workbookType)) {
                return new HSSFWorkbook(inputStream);
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "VResolverRead 解读 Workbook 工作簿异常", e);
            throw new RuntimeException("VResolverRead 解读 Workbook 工作簿异常", e);
        }
        return null;
    }


    /**
     * 解读指定的Sheet页为指定的对象列表
     *
     * @param workbook  工作簿对象
     * @param sheetName sheet页名称
     * @param tClass    接收数据的实体对象类型
     * @param <T>
     * @return
     */
    public static <T> List<T> readData(Workbook workbook, String sheetName, Class<T> tClass) {
        if (workbook == null) return null;
        if (tClass == null) throw new NullPointerException("读取工作表数据失败，未指定数据Class类型");
        if (sheetName == null || "".equals(sheetName.trim()))
            throw new NullPointerException("读取工作表数据失败，SheetName is null");

        VExcelTable vExcelTable = tClass.getDeclaredAnnotation(VExcelTable.class);
        if (vExcelTable == null) return null;

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new NullPointerException(String.format("读取工作表数据失败，Sheet 页[%s]不存在", sheetName));

        // 获取数据对象参数字典  key：VExcelTable.value
        Map<String, Field> fieldMap = new HashMap<>();
        Field fieldRowNumber = null;  // 实体类中指定了记录数据行号的字段
        for (Field field : tClass.getDeclaredFields()) {
            VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
            if (vExcelCell == null) continue;
            if (vExcelCell.isRowNumber()) {
                fieldRowNumber = field;
                continue;
            }
            fieldMap.put("".equals(vExcelCell.value()) ? field.getName() : vExcelCell.value(), field);
        }

        // 获取表头行
        int titleRowNumber = getTitleRowNumber(sheet, vExcelTable.titleRowNumber());
        List<String> titleNames = new ArrayList<>();
        Row row = sheet.getRow(titleRowNumber);
        if (isRowEmpty(row))
            throw new RuntimeException(String.format("解读Excel数据失败，无法确定表头所在行,请检查Sheet页[%s]第%s行数据", sheetName, titleRowNumber + 1));
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (isCellEmpty(cell))
                throw new RuntimeException(String.format("解读Excel数据失败，表头不完整,请检查Sheet页[%s]第%s行%s列数据"
                        , sheetName, titleRowNumber + 1, getColumnName(i + 1)));

            Object val = parseCellValueByType(cell);
            if ("String".equals(val.getClass().getSimpleName())) {
                titleNames.add(val.toString());
            } else {
                titleNames.add(null);
            }
        }

        // 解读数据
        List<T> result = new ArrayList<>();
        for (int i = titleRowNumber + 1; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            if (isRowEmpty(row)) continue;
            try {
                T bean = tClass.newInstance();
                result.add(bean);

                // 设置数据行号
                if (fieldRowNumber != null) setFieldValueByType(fieldRowNumber, bean, i + 1);

                for (int x = 0; x < row.getLastCellNum(); x++) {
                    if (x > titleNames.size() - 1 || titleNames.get(x) == null) continue;

                    Cell cell = row.getCell(x);
                    if (isCellEmpty(cell)) continue;

                    Field field = fieldMap.get(titleNames.get(x));
                    Object val = parseCellValueByType(cell);
                    setFieldValueByType(field, bean, val);
                }

            } catch (InstantiationException | IllegalAccessException e) {
                log.log(Level.WARNING, "解读Excel数据对象创建异常", e);
                throw new RuntimeException("解读Excel数据对象创建失败", e);
            }
        }

        return result;
    }

    /**
     * 获取表头所在 行
     *
     * @param sheet          sheet页对象
     * @param titleRowNumber 表头所在行
     * @return
     */
    public static int getTitleRowNumber(Sheet sheet, int titleRowNumber) {

        Row row = sheet.getRow(titleRowNumber == -1 || sheet.getLastRowNum() < titleRowNumber ? sheet.getFirstRowNum() : titleRowNumber);
        for (int i = row.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            off:
            if (row.getLastCellNum() == sheet.getRow(i).getLastCellNum()) {
                for (int x = row.getFirstCellNum(); x < row.getLastCellNum(); x++) {
                    // 当前行 有一个单元格为空则视为非表头行
                    if (isCellEmpty(row.getCell(x))) {
                        row = sheet.getRow(i);
                        break off;
                    }
                }
                return row.getRowNum();
            }
        }
        return titleRowNumber == -1 ? sheet.getFirstRowNum() : titleRowNumber;
    }

    /**
     * 验证行是否是空
     *
     * @param row
     * @return 本行有一个单元格有值则返回false 否则返回true
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (!isCellEmpty(row.getCell(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证单元格是否是空
     *
     * @param cell
     * @return 返回true为空, false不为空
     */
    public static boolean isCellEmpty(Cell cell) {
        boolean validate = true;
        if (cell != null && !CellType.BLANK.equals(cell.getCellType())) {
            if (CellType.NUMERIC.equals(cell.getCellType())) {
                validate = false;
            } else if (CellType.FORMULA.equals(cell.getCellType()) && cell.getCellFormula() != null && !"".equals(cell.getCellFormula().trim())) {
                return false;
            } else {
                if (!"".equals(cell.getStringCellValue().trim())) {
                    validate = false;
                }
            }
        }
        return validate;
    }

    /**
     * 解析单元格数据 根据类型转换
     *
     * @param cell 单元格对象
     * @return
     */
    public static Object parseCellValueByType(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case BLANK: // 空白单元格
                return null;
            case STRING: // 字符串(文本)单元格类型
                return cell.getStringCellValue();
            case NUMERIC: // 数值型单元格类型(整数、小数、日期)
                Date date = parseCellTypeOfDate(cell);
                if (date != null) return date;
                return cell.getNumericCellValue();
            case BOOLEAN: // Boolean
                return cell.getBooleanCellValue();
            case ERROR: // 异常单元格数据
                return null;
            case FORMULA: // 公式单元格，需要公式计算后得到值
                return parseCellTypeOfFormula(cell);
            default:
                return null;
        }
    }

    /**
     * 解析单元格日期时间数据
     *
     * @param cell 单元格对象
     * @return
     */
    public static Date parseCellTypeOfDate(Cell cell) {
        // General 常规
        // 日期时间类型的单元格样式
        List<Integer> dataFormatOfDate = Arrays.asList(14, 31, 57, 58, 164, 176, 177, 178, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213);

        // 获得单元格时间格式样式
        short format = cell.getCellStyle().getDataFormat();
        // 判断是否为日期
        if ((cell.getSheet().getWorkbook() instanceof HSSFWorkbook && HSSFDateUtil.isCellDateFormatted(cell))
                || (cell.getSheet().getWorkbook() instanceof XSSFWorkbook && DateUtil.isCellDateFormatted(cell))
                || dataFormatOfDate.contains((int) format)) {

            return DateUtil.getJavaDate(cell.getNumericCellValue());
        }
        return null;
    }

    /**
     * 解析单元格公式
     *
     * @param cell 单元格对象
     * @return
     */
    public static Object parseCellTypeOfFormula(Cell cell) {
        // 公式,解读单元格公式计算后的值
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        CellValue c = evaluator.evaluate(cell);
        switch (c.getCellType()) {
            case STRING: // 字符串
                return c.getStringValue();
            case NUMERIC: // 数字
                return c.getNumberValue();
            default:
                return null;
        }
    }

    /**
     * 根据字段与值类型匹配给字段赋值
     *
     * @param field 字段对象
     * @param val   值
     */
    public static void setFieldValueByType(Field field, Object bean, Object val) {
        if (field == null || bean == null || val == null) return;
        try {
            VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
            field.setAccessible(true);
            if (field.getType().getSimpleName().equals(val.getClass().getSimpleName())) {
                field.set(bean, val);
            } else {
                switch (field.getType().getSimpleName()) {
                    case "String":
                        val = val.toString();
                        break;
                    case "Integer":
                        val = (int) Double.parseDouble(val.toString());
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
                    case "Date":
                        if ("String".equals(val.getClass().getSimpleName())) {
                            SimpleDateFormat simpleDateFormat = null;
                            if (VExcelCellFormatEnum.FORMAT_DATE_TIME.equals(vExcelCell.cellFormat())) {
                                simpleDateFormat = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
                            } else if (!"".equals(vExcelCell.cellFormatCustom())) {
                                simpleDateFormat = new SimpleDateFormat(vExcelCell.cellFormatCustom());
                            }
                            if (simpleDateFormat == null) {
                                val = null;
                                break;
                            }
                            val = simpleDateFormat.parse(val.toString());
                        }
                        break;
                    default:
                }

                if (val != null)
                    field.set(bean, val);
            }
        } catch (IllegalAccessException | ParseException | IllegalArgumentException e) {
            String msg = String.format("字段赋值异常, 对象类型:%s  属性:%s  类型:%s  值:%s", bean.getClass().getName(), field.getName(), field.getType().getName(), val);
            log.log(Level.WARNING, msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * 获取指定的单元格列数对应的字母列名
     *
     * @param columnNum
     * @return
     */
    public static String getColumnName(int columnNum) {
        String columnName = "";
        if (columnNum > 26) {
            if (columnNum % 26 == 0 && columnNum / 26 < 26) {
                columnName += (char) (columnNum / 26 - 1 + 64); // 字母A 为65
                columnName += 'Z';
            } else {
                columnName += getColumnName(columnNum / 26);
                columnName += (char) (columnNum % 26 + 64); // 字母A 为65
            }
        } else {
            columnName += (char) (columnNum + 64);  // 字母A 为65
        }

        return columnName;
    }
}
