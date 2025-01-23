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

    /**
     * 大数据量导出
     *
     * @param fileName 文件名称
     * @param dataList 数据
     * @param response 响应
     */
    public static void exportBigData(String fileName, List<JSONObject> dataList, HttpServletResponse response) {
        XlsxFileDownloader.download(exportBigExcel(dataList), fileName, response);
    }

    public static void exportBigData(String filePath, List<JSONObject> dataList) {
        XlsxFileWriter.writeWorkbookToFile(exportBigExcel(dataList), filePath);
    }

    private static SXSSFWorkbook exportBigExcel(List<JSONObject> dataList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("Sheet1");
        if (CollectionUtils.isEmpty(dataList)) {
            return workbook;
        }
        int rowIndex = 0;
        //初始化标题行
        JSONObject data = dataList.get(rowIndex);
        Row titleRow = sheet.createRow(rowIndex);
        List<String> title = new ArrayList<>();
        int index = 0;
        for (String key : data.keySet()) {
            titleRow.createCell(index).setCellValue(key);
            title.add(key);
            index++;
        }
        //初始化数据行
        for (JSONObject obj : dataList) {
            rowIndex++;
            Row row = sheet.createRow(rowIndex);
            for (int i = 0; i < title.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(obj.getString(title.get(i)));
            }
        }
        return workbook;
    }

}
