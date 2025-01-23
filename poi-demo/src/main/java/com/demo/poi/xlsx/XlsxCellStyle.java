package com.demo.poi.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.EnumMap;
import java.util.Map;

public class XlsxCellStyle {

    //初始化样式map
    public static Map<XlsxCellStyleType, XSSFCellStyle> initCellStyle(XSSFWorkbook workbook) {
        Map<XlsxCellStyleType, XSSFCellStyle> map = new EnumMap<>(XlsxCellStyleType.class);
        map.put(XlsxCellStyleType.TITLE, initTitleStyle(workbook));
        map.put(XlsxCellStyleType.NORMAL, initNormalStyle(workbook));
        map.put(XlsxCellStyleType.READONLY, initReadOnlyDataStyle(workbook));
        map.put(XlsxCellStyleType.ERROR, initErrorDataStyle(workbook));
        map.put(XlsxCellStyleType.RED_FONT, initRedFontStyle(workbook));
        map.put(XlsxCellStyleType.LEFT_TOP, initLeftTopStyle(workbook));
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
