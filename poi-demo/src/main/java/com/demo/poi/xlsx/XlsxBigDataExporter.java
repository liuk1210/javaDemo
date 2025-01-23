package com.demo.poi.xlsx;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XlsxBigDataExporter {

    private static final int ROW_ACCESS_WINDOW_SIZE = 100; // 设置SXSSFWorkbook的行缓存大小

    /**
     * 大数据量导出到HTTP响应
     *
     * @param fileName 文件名称
     * @param dataList 数据列表
     * @param response HTTP响应
     */
    public static void exportBigData(String fileName, List<JSONObject> dataList, HttpServletResponse response) {
        SXSSFWorkbook workbook = createWorkbook(dataList);
        XlsxFileDownloader.download(workbook, fileName, response);
    }

    /**
     * 大数据量导出到本地文件
     *
     * @param filePath 文件路径
     * @param dataList 数据列表
     */
    public static void exportBigData(String filePath, List<JSONObject> dataList) {
        SXSSFWorkbook workbook = createWorkbook(dataList);
        XlsxFileWriter.writeWorkbookToFile(workbook, filePath);
    }

    /**
     * 创建SXSSFWorkbook并填充数据
     *
     * @param dataList 数据列表
     * @return 填充数据后的SXSSFWorkbook
     */
    private static SXSSFWorkbook createWorkbook(List<JSONObject> dataList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        Sheet sheet = workbook.createSheet("Sheet1");

        if (CollectionUtils.isEmpty(dataList)) {
            return workbook;
        }

        // 初始化标题行
        List<String> titles = createTitleRow(sheet, dataList.get(0));

        // 填充数据行
        fillDataRows(sheet, dataList, titles);

        return workbook;
    }

    /**
     * 创建标题行
     *
     * @param sheet  工作表
     * @param sampleData 样本数据（用于提取标题）
     * @return 标题列表
     */
    private static List<String> createTitleRow(Sheet sheet, JSONObject sampleData) {
        Row titleRow = sheet.createRow(0);
        List<String> titles = new ArrayList<>();

        int cellIndex = 0;
        for (String key : sampleData.keySet()) {
            Cell cell = titleRow.createCell(cellIndex++);
            cell.setCellValue(key);
            titles.add(key);
        }

        return titles;
    }

    /**
     * 填充数据行
     *
     * @param sheet    工作表
     * @param dataList 数据列表
     * @param titles   标题列表
     */
    private static void fillDataRows(Sheet sheet, List<JSONObject> dataList, List<String> titles) {
        int rowIndex = 1; // 数据从第二行开始

        for (JSONObject data : dataList) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < titles.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(data.getString(titles.get(i)));
            }
        }
    }

}
