package com.demo.poi.xlsx.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;

public class XlsxCellStyle {

    /**
     * 单元格样式
     */
    public static final String STYLE_NORMAL = "normal",
            STYLE_TITLE = "title",
            STYLE_READONLY = "readOnly",
            STYLE_ERROR = "error",
            STYLE_RED_FONT = "redFont",
            STYLE_LEFT_TOP = "leftTop";

    //初始化样式map
    public static Map<String, XSSFCellStyle> initCellStyle(XSSFWorkbook workbook) {
        Map<String, XSSFCellStyle> map = new HashMap<>();
        map.put(STYLE_TITLE, initTitleStyle(workbook));
        map.put(STYLE_NORMAL, initNormalStyle(workbook));
        map.put(STYLE_READONLY, initReadOnlyDataStyle(workbook));
        map.put(STYLE_ERROR, initErrorDataStyle(workbook));
        map.put(STYLE_RED_FONT, initRedFontStyle(workbook));
        map.put(STYLE_LEFT_TOP, initLeftTopStyle(workbook));
        return map;
    }

    private static XSSFCellStyle initTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN); // 上边框
        style.setBorderBottom(BorderStyle.THIN); // 下边框
        style.setBorderLeft(BorderStyle.THIN); // 左边框
        style.setBorderRight(BorderStyle.THIN); // 右边框
        return style;
    }

    private static XSSFCellStyle initRedFontStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);

        return style;
    }

    private static XSSFCellStyle initNormalStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle initLeftTopStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private static XSSFCellStyle initErrorDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle initReadOnlyDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

}
