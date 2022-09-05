package com.ck.v_excel;

import com.ck.v_excel.annotation.VExcelCell;
import com.ck.v_excel.annotation.VExcelTable;
import com.ck.v_excel.annotation.VExcelTitleStyle;
import com.ck.v_excel.enums.VExcelStyle;
import com.ck.v_excel.enums.VExcelWorkbookType;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestVExcel {
    public static void main(String[] args) {
        List<ExcelBean> excelBeans = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            excelBeans.add(new ExcelBean()
                    .setName("测试人员" + i)
                    .setAge((int) (Math.random() * 10 + i))
                    .setSex(Math.random() > 0.5 ? "男" : "女")
                    .setCertType("身份证")
                    .setCertNo("5452132321564531351132")
                    .setAddress("北京市朝阳区随机社区你猜小区不知道单元")
                    .setPhone("4154523232").setSalary(new BigDecimal((int) (Math.random() * 100 + i))).setBirthday(new Date()));

        }

        VExcel vExcel = VExcel.getInstance(excelBeans);
//        vExcel.write(new File("C:\\Users\\ck\\Desktop\\新建文件夹\\测试Excel.xlsx"));
//        vExcel.write(new File("C:\\Users\\cyk\\Desktop\\fsdownload\\测试Excel.xlsx"));
//        vExcel.setWorkbookType(VExcelWorkbookType.CSV).write(new File("C:\\Users\\cyk\\Desktop\\fsdownload\\测试CSV.csv"));

//        vExcel.read(VExcelWorkbookType.XLS_X, new File("C:\\Users\\ck\\Desktop\\新建文件夹\\测试Excel.xlsx"));
        vExcel.read(VExcelWorkbookType.XLS_X, new File("C:\\Users\\cyk\\Desktop\\fsdownload\\测试Excel.xlsx"));
//        vExcel.read(VExcelWorkbookType.CSV, new File("C:\\Users\\cyk\\Desktop\\fsdownload\\测试CSV.csv"));
        excelBeans = vExcel.readData("测试人员", ExcelBean.class);
        excelBeans = vExcel.readData(1, ExcelBean.class);
        List<List<ExcelBean>> result = vExcel.readData(ExcelBean.class);
        System.out.println(excelBeans);
        System.out.println(result);

    }


    @VExcelTable(value = "测试人员列表", sheetName = "测试人员", titleRowNumber = 2)
    @Data
    @Accessors(chain = true)
    private static class ExcelBean {
        @VExcelTitleStyle(fontColor = VExcelStyle.ColorEnum.AQUA)
        @VExcelCell(value = "姓名", fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = VExcelStyle.ColorEnum.LIGHT_CORNFLOWER_BLUE)
        private String name;
        @VExcelTitleStyle
        @VExcelCell(value = "年龄", fontColor = VExcelStyle.ColorEnum.AUTOMATIC)
        private Integer age;
        @VExcelTitleStyle
        @VExcelCell(value = "性别", fontColor = VExcelStyle.ColorEnum.BLUE, border = VExcelStyle.BorderEnum.MEDIUM)
        private String sex;
        @VExcelTitleStyle
        @VExcelCell(value = "证件类型", fontColor = VExcelStyle.ColorEnum.LIGHT_TURQUOISE, border = VExcelStyle.BorderEnum.MEDIUM)
        private String certType;
        @VExcelTitleStyle
        @VExcelCell(value = "证件号", autoSizeColumn = true, fontColor = VExcelStyle.ColorEnum.LIGHT_CORNFLOWER_BLUE)
        private String certNo;
        @VExcelTitleStyle
        @VExcelCell(value = "地址", fontColor = VExcelStyle.ColorEnum.ROYAL_BLUE)
        private String address;
        @VExcelTitleStyle
        @VExcelCell(value = "邮箱", fontColor = VExcelStyle.ColorEnum.GREY_40_PERCENT)
        private String emil;
        @VExcelTitleStyle
        @VExcelCell(value = "电话", fontColor = VExcelStyle.ColorEnum.AQUA)
        private String phone;
        @VExcelCell(value = "工资", fontColor = VExcelStyle.ColorEnum.AQUA)
        private BigDecimal salary;
        @VExcelCell(value = "生日", autoSizeColumn = true, fontColor = VExcelStyle.ColorEnum.AQUA)
        private Date birthday;

        @VExcelCell(isRowNumber = true)
        private int index;
    }
}


