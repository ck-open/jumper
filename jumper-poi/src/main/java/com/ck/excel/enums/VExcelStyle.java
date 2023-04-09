package com.ck.excel.enums;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;

import java.awt.*;

/**
 * @author Cyk
 * @description Excel 边框样式
 * @since 2022/9/4 18:30
 **/
public class VExcelStyle {

    /**
     * 自定义颜色
     * @param v  需要数组三个参数 依次为{R,G,B}
     * @return
     */
    public static HSSFColor FromColor(int... v){
        if (v==null || v.length<2) throw new RuntimeException("创建 HSSFColor 需要三个参数 依次为{R,G,B}");
        return  FromColor(new Color(v[0],v[1],v[2]));
    }
    /**
     * 自定义颜色
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static HSSFColor FromColor(int r, int g, int b){
        return  FromColor(new Color(r,g,b));
    }

    /**
     * 自定义颜色
     * @param color
     * @return
     */
    public static HSSFColor FromColor(Color color){
        return new HSSFColor(0x40, -1,color);
    }

    /**
     * 单元格边框样式
     */
    public static enum BorderEnum {
        NONE("NONE","无样式",BorderStyle.NONE),
        THIN("THIN","细实线",BorderStyle.THIN),
        MEDIUM("","单元格边框样式",BorderStyle.MEDIUM),
        DASHED("DASHED","破折线",BorderStyle.DASHED),
        DOTTED("DOTTED","点虚线",BorderStyle.DOTTED),
        THICK("THICK","粗实线",BorderStyle.THICK),
        DOUBLE("DOUBLE","双线",BorderStyle.DOUBLE),
        HAIR("","单元格边框样式",BorderStyle.HAIR),
        MEDIUM_DASHED("","单元格边框样式",BorderStyle.MEDIUM_DASHED),
        DASH_DOT("","单元格边框样式",BorderStyle.DASH_DOT),
        MEDIUM_DASH_DOT("","单元格边框样式",BorderStyle.MEDIUM_DASH_DOT),
        DASH_DOT_DOT("","单元格边框样式",BorderStyle.DASH_DOT_DOT),
        MEDIUM_DASH_DOT_DOT("","单元格边框样式",BorderStyle.MEDIUM_DASH_DOT_DOT),
        SLANTED_DASH_DOT("","单元格边框样式",BorderStyle.SLANTED_DASH_DOT);

        private String code;
        private String name;
        private String message;
        /**
         * 边框样式
         */
        private BorderStyle borderStyle;

        BorderEnum(String name, String message, BorderStyle borderStyle) {
            this.name = name;
            this.message = message;
            this.borderStyle = borderStyle;
        }

        BorderEnum(String code, String name, String message) {
            this.code = code;
            this.name = name;
            this.message = message;
        }


        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }

        public BorderStyle getBorderStyle() {
            return borderStyle;
        }

    }

    public enum ColorTypeEnum{
        NONE(-1,"不使用"),
        FONT(0,"字体颜色"),
        BORDER(1,"颜色"),
        FILL_FOREGROUND(2,"单元格背景填充颜色"),
        ;
        Integer colorType;
        String colorTypeName;

        ColorTypeEnum(Integer colorType, String colorTypeName) {
            this.colorType = colorType;
            this.colorTypeName = colorTypeName;
        }

        public Integer getColorType() {
            return colorType;
        }

        public String getColorTypeName() {
            return colorTypeName;
        }
    }

    /**
     * 颜色枚举
     */
    public enum ColorEnum {
        AUTOMATIC("AUTOMATIC","无",HSSFColor.HSSFColorPredefined.AUTOMATIC.getColor()),
        GREY_80_PERCENT("GREY_80_PERCENT","80%灰色",HSSFColor.HSSFColorPredefined.GREY_80_PERCENT.getColor()),
        GREY_50_PERCENT("GREY_50_PERCENT","50%灰色",HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getColor()),
        GREY_40_PERCENT("GREY_40_PERCENT","40%灰色",HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getColor()),
        GREY_25_PERCENT("GREY_25_PERCENT","25%灰色",HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getColor()),
        BLACK("BLACK","黑色", HSSFColor.HSSFColorPredefined.BLACK.getColor()),
        BROWN("BROWN","褐色",HSSFColor.HSSFColorPredefined.BROWN.getColor()),
        OLIVE_GREEN("OLIVE_GREEN","橄榄绿",HSSFColor.HSSFColorPredefined.OLIVE_GREEN.getColor()),
        DARK_GREEN("DARK_GREEN","深绿色",HSSFColor.HSSFColorPredefined.DARK_GREEN.getColor()),
        DARK_TEAL("DARK_TEAL","深蓝绿",HSSFColor.HSSFColorPredefined.DARK_TEAL.getColor()),
        DARK_BLUE("DARK_BLUE","深蓝色",HSSFColor.HSSFColorPredefined.DARK_BLUE.getColor()),
        INDIGO("INDIGO","靛蓝色",HSSFColor.HSSFColorPredefined.INDIGO.getColor()),
        ORANGE("ORANGE","橘黄色",HSSFColor.HSSFColorPredefined.ORANGE.getColor()),
        DARK_YELLOW("DARK_YELLOW","深黄色",HSSFColor.HSSFColorPredefined.DARK_YELLOW.getColor()),
        GREEN("GREEN","绿色",HSSFColor.HSSFColorPredefined.GREEN.getColor()),
        TEAL("TEAL","青色",HSSFColor.HSSFColorPredefined.TEAL.getColor()),
        BLUE("BLUE","蓝色",HSSFColor.HSSFColorPredefined.BLUE.getColor()),
        BLUE_GREY("BLUE_GREY","蓝灰色",HSSFColor.HSSFColorPredefined.BLUE_GREY.getColor()),
        RED("RED","红色",HSSFColor.HSSFColorPredefined.RED.getColor()),
        LIGHT_ORANGE("LIGHT_ORANGE","淡橙色",HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getColor()),
        LIME("LIME","橙绿色",HSSFColor.HSSFColorPredefined.LIME.getColor()),
        SEA_GREEN("SEA_GREEN","海绿色",HSSFColor.HSSFColorPredefined.SEA_GREEN.getColor()),
        AQUA("AQUA","浅绿色",HSSFColor.HSSFColorPredefined.AQUA.getColor()),
        LIGHT_BLUE("LIGHT_BLUE","淡蓝色",HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getColor()),
        PALE_BLUE("PALE_BLUE","淡蓝色",HSSFColor.HSSFColorPredefined.PALE_BLUE.getColor()),
        VIOLET("VIOLET","紫色",HSSFColor.HSSFColorPredefined.VIOLET.getColor()),
        PINK("PINK","粉红色",HSSFColor.HSSFColorPredefined.PINK.getColor()),
        GOLD("GOLD(","金色",HSSFColor.HSSFColorPredefined.GOLD.getColor()),
        YELLOW("YELLOW","黄色",HSSFColor.HSSFColorPredefined.YELLOW.getColor()),
        BRIGHT_GREEN("BRIGHT_GREEN","鲜绿色",HSSFColor.HSSFColorPredefined.BRIGHT_GREEN.getColor()),
        TURQUOISE("TURQUOISE","蓝绿色",HSSFColor.HSSFColorPredefined.TURQUOISE.getColor()),
        DARK_RED("DARK_RED","深红色",HSSFColor.HSSFColorPredefined.DARK_RED.getColor()),
        SKY_BLUE("SKY_BLUE","颜色",HSSFColor.HSSFColorPredefined.SKY_BLUE.getColor()),
        PLUM("PLUM","紫红色",HSSFColor.HSSFColorPredefined.PLUM.getColor()),
        ROSE("ROSE","玫瑰色",HSSFColor.HSSFColorPredefined.ROSE.getColor()),
        LIGHT_YELLOW("LIGHT_YELLOW","淡黄色",HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getColor()),
        LIGHT_GREEN("LIGHT_GREEN","淡绿色",HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getColor()),
        LIGHT_TURQUOISE("LIGHT_TURQUOISE","颜色",HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getColor()),
        LAVENDER("LAVENDER","薰衣草色",HSSFColor.HSSFColorPredefined.LAVENDER.getColor()),
        WHITE("WHITE","白色",HSSFColor.HSSFColorPredefined.WHITE.getColor()),
        CORNFLOWER_BLUE("CORNFLOWER_BLUE","矢菊花蓝",HSSFColor.HSSFColorPredefined.CORNFLOWER_BLUE.getColor()),
        LEMON_CHIFFON("LEMON_CHIFFON","柠檬色",HSSFColor.HSSFColorPredefined.LEMON_CHIFFON.getColor()),
        MAROON("MAROON","褐红色",HSSFColor.HSSFColorPredefined.MAROON.getColor()),
        ORCHID("ORCHID","淡紫色",HSSFColor.HSSFColorPredefined.ORCHID.getColor()),
        CORAL("CORAL","珊瑚色",HSSFColor.HSSFColorPredefined.CORAL.getColor()),
        ROYAL_BLUE("ROYAL_BLUE","品蓝",HSSFColor.HSSFColorPredefined.ROYAL_BLUE.getColor()),
        LIGHT_CORNFLOWER_BLUE("LIGHT_CORNFLOWER_BLUE","颜色",HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getColor()),
        TAN("TAN","棕褐色",HSSFColor.HSSFColorPredefined.TAN.getColor()),
        ;


        private String code;
        private String name;
        /**
         * 颜色
         */
        private HSSFColor color;

        ColorEnum(String code, String name, HSSFColor color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }
        public HSSFColor getColor() {
            return color;
        }
    }
}
