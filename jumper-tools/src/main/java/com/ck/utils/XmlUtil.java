package com.ck.utils;

import java.util.List;

/**
 * @ClassName XmlUtil
 * @Description 标签工具
 * @Author Cyk
 * @Version 1.0
 * @since 2022/4/15 17:09
 **/
public final class XmlUtil {

    /**
     * @description 返回标签列表字符串
     * 原用于邮件中的表格生成
     * @author Cyk
     * @since 17:10 2022/4/15
     * @param rows
     * @param title
     * @return String
     **/
    private String createTable(List<List<String >> rows, List<String> title){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;font-size:14px; border-collapse: collapse; margin-left:50px;\">");
        // 表头
        content.append("<tr style=\"background-color: #778899; color:#ffffff; text-align:center; width:1400px;\">");
        for (String t : title){
            content.append("<th style=\"padding: 5px;\">"+t+"</th>");
        }
        content.append("</tr>");
        // table body
        for (List<String> row : rows) {
            content.append("<tr>");
            for (String cell : row) {
                content.append("<td style=\"padding: 5px;\">" + cell + "</td>");
            }
            content.append("</tr>");
        }
        content.append("</table>");
        return content.toString();
    }
}
