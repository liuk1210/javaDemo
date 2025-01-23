package com.demo.poi.xlsx;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XlsxReader {

    public static List<JSONObject> read(MultipartFile file, int titleStartRow, int titleRowNum) {
        return readData(file, titleStartRow, titleRowNum, null);
    }

    /**
     * 读取多行表头的数据
     *
     * @param file          文件
     * @param titleStartRow 头开始行，从0开始
     * @param title         表头列信息
     * @return 表头为key的json数组  多行表头中间以 - 分割
     */
    public static List<JSONObject> read(MultipartFile file, int titleStartRow, List<List<XlsxCell>> title) {
        if (file == null || file.isEmpty()) {
            return new ArrayList<>();
        }
        return readData(file, titleStartRow, title.size(), title);
    }

    //读取文件，title不为空就校验表头行
    public static List<JSONObject> readData(MultipartFile file, int titleStartRow, int titleRowNum, List<List<XlsxCell>> title) {
        List<JSONObject> rs = new ArrayList<>();
        if (file == null || file.isEmpty()) {
            return rs;
        }
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("IO异常，读取上传文件失败！");
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        if (titleStartRow != 0 && sheet.getPhysicalNumberOfRows() <= titleStartRow) {
            throw new RuntimeException("读取文件失败，请勿删除模板样式行！");
        }

        if (sheet.getPhysicalNumberOfRows() <= (1 + titleStartRow)) {//只有一行时默认为表头行不返回任何内容
            return rs;
        }

        if (title != null && !title.isEmpty()) {
            //如果传入了表头行数据，则校验表头行并获取数据，表头行不正确则报错,表头列数以第一行为准
            String[] titleKey = new String[title.get(0).size()];
            //校验表头行数据是否正确
            for (int i = 0; i < title.size(); i++) {
                Row titleRow = sheet.getRow(titleStartRow + i);
                List<XlsxCell> xlsxCells = title.get(i);
                for (int j = 0; j < xlsxCells.size(); j++) {
                    XlsxCell xlsxCell = xlsxCells.get(j);
                    String titleRowCellValue = getMergedCellValue(sheet, titleRow, j);
                    if (!StringUtils.equals(xlsxCell.getValue(), titleRowCellValue)) {
                        throw new RuntimeException("导入模板有误，请勿修改模板表头行！");
                    }
                    if (i == title.size() - 1) {
                        titleKey[j] = xlsxCell.getKey();
                    }
                }
            }
            initDataRow(sheet, rs, titleStartRow + titleRowNum, titleKey);
        } else {
            //如果没有传表头行，则按照表头行数取值
            Row tr = sheet.getRow(titleStartRow);
            if (tr == null) {
                return rs;
            }
            int titleLength = tr.getLastCellNum();
            String[] titleValues = new String[titleLength];
            for (int i = 0; i < titleRowNum; i++) {
                Row titleRow = sheet.getRow(titleStartRow + i);
                for (int j = 0; j < titleLength; j++) {
                    DataFormatter formatter = new DataFormatter();
                    String titleRowCellValue = formatter.formatCellValue(titleRow.getCell(j));
                    titleValues[j] = titleValues[j] == null ? titleRowCellValue : titleValues[j] + "|" + titleRowCellValue;
                }
            }
            String[] titles = new String[titleValues.length];
            for (int i = 0; i < titleValues.length; i++) {
                titles[i] = trimAndRemoveEmptyPipes(titleValues[i]);
            }
            initDataRow(sheet, rs, titleStartRow + titleRowNum, titles);
        }
        try {
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return rs;
    }

    /**
     * 获取单元格的值（支持合并单元格）
     *
     * @param sheet 工作表
     * @param row   行
     * @param col   列索引
     * @return 单元格的值，如果单元格为空则返回空字符串
     */
    public static String getMergedCellValue(Sheet sheet, Row row, int col) {
        // 获取单元格
        Cell cell = row.getCell(col);
        if (cell != null) {
            DataFormatter formatter = new DataFormatter();
            // 检查单元格是否属于合并区域
            for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
                if (mergedRegion.isInRange(row.getRowNum(), col)) {
                    // 如果是合并区域，返回左上角单元格的值
                    Row firstRow = sheet.getRow(mergedRegion.getFirstRow());
                    Cell firstCell = firstRow.getCell(mergedRegion.getFirstColumn());
                    return firstCell != null ? formatter.formatCellValue(firstCell) : "";
                }
            }
            // 如果不是合并区域，直接返回单元格的值
            return formatter.formatCellValue(cell);
        }
        // 如果单元格为空，返回空字符串
        return "";
    }

    private static void initDataRow(Sheet sheet, List<JSONObject> rs, int titleEndRow, String[] titleValues) {
        for (int i = titleEndRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            JSONObject obj = new JSONObject();
            for (int j = 0; j < row.getLastCellNum(); j++) {
                if (j >= titleValues.length) {
                    //不获取超出标题行的数据
                    continue;
                }
                Cell cell = row.getCell(j);
                if (cell != null) {
                    DataFormatter formatter = new DataFormatter();
                    String cellValue = formatter.formatCellValue(cell);
                    String titleRowCellValue = titleValues[j];
                    obj.put(titleRowCellValue, cellValue);
                }
            }
            rs.add(obj);
        }
    }

    /**
     * 去除字符串首尾的 | 以及中间为空的 ||
     *
     * @param input 输入字符串
     * @return 处理后的字符串
     */
    public static String trimAndRemoveEmptyPipes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 去除首尾的 |
        input = input.replaceAll("^\\|+|\\|+$", "");
        // 去除中间为空的 ||
        input = input.replaceAll("\\|\\|", "|");
        return input;
    }

}
