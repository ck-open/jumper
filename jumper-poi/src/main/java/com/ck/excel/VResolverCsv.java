package com.ck.excel;

import com.ck.excel.annotation.VExcelCell;
import com.ck.excel.annotation.VExcelTable;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName VResolverCsv
 * @Description 逗号分割文档数据解析器 读写操作
 * @Author Cyk
 * @Version 1.0
 * @since 2022/9/4 18:30
 **/
public class VResolverCsv {
    private static Logger log = Logger.getLogger(VResolverCsv.class.getName());

    /**
     * 数据写出到 CSV 文件
     *
     * @param data         数据
     * @param outputStream 输出流
     * @param charsets     字符集 默认 UTF-8
     */
    public static void writer(List<?> data, OutputStream outputStream, Charset charsets) {
        if (data == null || outputStream == null) throw new NullPointerException("Data 与 OutputStream 不能为空");
        if (charsets == null) charsets = StandardCharsets.UTF_8;
        if (data.size() < 1) return;

        VExcelTable vExcelTable = data.get(0).getClass().getDeclaredAnnotation(VExcelTable.class);
        if (vExcelTable == null) return;

        int titleRowNum = vExcelTable.titleRowNumber() == -1 ? 0 : vExcelTable.titleRowNumber();


        //创建csv输出流
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, charsets.name()))) {
            if (titleRowNum > 0) {
                for (int i = 0; i < titleRowNum; i++) bw.newLine();
                bw.flush();
            }
            Map<String, SimpleDateFormat> simpleDateFormatMap = new HashMap<>(); // 日期格式
            List<Field> vExcelCellField = new ArrayList<>();
            Field[] fields = data.get(0).getClass().getDeclaredFields();
            boolean isFirst = true;
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
                if (vExcelCell != null && !vExcelCell.isRowNumber()) {
                    vExcelCellField.add(field);

                    if (Date.class.getSimpleName().equals(field.getType().getSimpleName())) {
                        simpleDateFormatMap.put(field.getName(), new SimpleDateFormat("".equals(vExcelCell.cellFormatCustom().trim()) ? "yyyy-MM-dd HH:mm:ss" : vExcelCell.cellFormatCustom()));
                    }

                    // 不是第一列则添加分割符
                    if (!isFirst) bw.write(",");
                    if (isFirst) isFirst = false;

                    bw.write("".equals(vExcelCell.value().trim()) ? field.getName() : vExcelCell.value());
                }
            }
            // 表头写完 换行 并推送数据
            bw.newLine();
            bw.flush();

            // 遍历数据写出到文件
            for (int r = 0; r < data.size(); r++) {
                Object row = data.get(r);
                for (int c = 0; c < vExcelCellField.size(); c++) {
                    Field cellField = vExcelCellField.get(c);

                    cellField.setAccessible(true);
                    Object val = cellField.get(row);
                    if (val != null) {
                        if (Date.class.getSimpleName().equals(cellField.getType().getSimpleName())) {
                            val = simpleDateFormatMap.get(cellField.getName()).format(val);
                        } else {
                            val = VResolverStyle.resetObjectByFieldType(cellField, val);
                        }
                    }

                    if (val != null) {
                        // 长数字处理 避免Excel打开出现的科学计数法
                        String valTemp = val.toString();
                        if (valTemp.length() > 10 && valTemp.matches("^[+-]?((\\d{1,50})|(0{1}))(\\.\\d{1,})?$")) {
                            val = "`" + val.toString();
                        }
                        bw.write(val.toString());
                    }
                    if (c < vExcelCellField.size() - 1)
                        bw.write(",");
                }

                // 不是最后一行则添加行
                if (r != data.size())
                    bw.newLine();
            }
        } catch (IOException | IllegalAccessException e) {
            log.log(Level.WARNING, "CSV 文件写出异常", e);
            throw new RuntimeException("CSV 文件写出异常", e);
        }
    }

    /**
     * 读取 CSV 文件内容<br>
     * 每个元素代表一行数据,数组的每个元素代表每个单元格
     *
     * @param in      csv文件输入流
     * @param charset 文件字符集
     * @return
     */
    public static <T> List<T> read(InputStream in, Charset charset, Class<T> cla) {
        return read(read(in, charset), cla);
    }

    /**
     * CSV 文件内容解析为指定对象<br>
     * 每个元素代表一行数据,数组的每个元素代表每个单元格
     *
     * @param data 读取出的CSV文件内容
     * @param cla  文件字符集
     * @return
     */
    public static <T> List<T> read(List<List<String>> data, Class<T> cla) {
        if (cla == null) throw new NullPointerException("CSV 读取失败，未指定需要转换的Class 类型");
        List<T> result = new ArrayList<>();
        VExcelTable vExcelTable = cla.getDeclaredAnnotation(VExcelTable.class);
        if (vExcelTable == null) return result;

        Map<String, SimpleDateFormat> simpleDateFormatMap = new HashMap<>(); // 日期格式
        Map<String, Field> vExcelCellField = new LinkedHashMap<>();
        Field fieldRowNumber = null;  // 实体类中指定了记录数据行号的字段
        Field[] fields = cla.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            VExcelCell vExcelCell = field.getDeclaredAnnotation(VExcelCell.class);
            if (vExcelCell == null) continue;
            if (vExcelCell.isRowNumber()) {
                fieldRowNumber = field;
                continue;
            }
            vExcelCellField.put("".equals(vExcelCell.value().trim()) ? field.getName() : vExcelCell.value(), field);

            if (Date.class.getSimpleName().equals(field.getType().getSimpleName())) {
                simpleDateFormatMap.put(field.getName(), new SimpleDateFormat("".equals(vExcelCell.cellFormatCustom().trim()) ? "yyyy-MM-dd HH:mm:ss" : vExcelCell.cellFormatCustom()));
            }
        }

        if (!data.isEmpty()) {
            int titleRowNum = vExcelTable.titleRowNumber() == -1 ? 0 : vExcelTable.titleRowNumber();
            if (titleRowNum > data.size() - 2) return result;

            List<String> titles = data.get(titleRowNum);
            for (int r = titleRowNum + 1; r < data.size(); r++) {
                List<?> rowData = data.get(r);
                if (rowData.isEmpty()) continue;
                try {
                    T bean = cla.newInstance();
                    result.add(bean);
                    // 设置数据行号
                    if (fieldRowNumber != null) VResolverRead.setFieldValueByType(fieldRowNumber, bean, r + 1);
                    for (int c = 0; c < rowData.size(); c++) {
                        if (!vExcelCellField.containsKey(titles.get(c))) continue;

                        Field cellField = vExcelCellField.get(titles.get(c));
                        Object val = rowData.get(c);
                        if (val == null || "".equals(val)) continue;
                        try {
                            // 长数字处理
                            if (val.toString().startsWith("`")) val = val.toString().substring(1);

                            if (Date.class.getSimpleName().equals(cellField.getType().getSimpleName())) {
                                val = simpleDateFormatMap.get(cellField.getName()).parse(val.toString());
                            } else {
                                val = VResolverStyle.resetObjectByFieldType(cellField, val);
                            }
                        } catch (Exception e) {
                            log.info(String.format("CSV 值解析失败，第%s行%s列  Class:%s  FieldName:%s  FieldType:%s  Value:%s"
                                    , r, c, cla.getName(), cellField.getName(), cellField.getType().getName(), val.toString()));
                            continue;
                        }
                        cellField.setAccessible(true);
                        cellField.set(bean, val);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    log.log(Level.WARNING, "CSV 数据转换实例异常", e);
                    throw new RuntimeException("CSV 数据转换实例异常", e);
                }
            }
        }

        return result;
    }

    /**
     * 读取CSV文件内容<br>
     * 每个元素代表一行数据,数组的每个元素代表每个单元格
     *
     * @param in      csv文件输入流
     * @param charset 文件字符集
     * @return
     */
    public static List<List<String>> read(InputStream in, Charset charset) {
        if (in == null) throw new NullPointerException("CSV 文件读取失败，InputStream 为空");
        if (charset == null) charset = StandardCharsets.UTF_8;

        //声明List集合储存文件数据，String数组每个元素表示一个单元格
        List<List<String>> csv = new ArrayList<>();

        //创建文件输入流对象，读取.csv文件
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {

            //reader.readLine();//获取第一行 为标题信息
            String line = null;
            while ((line = reader.readLine()) != null) {//遍历文件所有数据
                String[] item = line.split(","); //以逗号分隔获取所有数据到数组
                if (item.length > 0)
                    csv.add(Arrays.asList(item)); //将读取到的行数据添加到list集合
            }
        } catch (IOException e) {
            //文件输入流异常
            log.log(Level.WARNING, "CSV 文件读取异常", e);
            throw new RuntimeException("CSV 文件读取异常", e);
        }
        return csv;
    }
}
