package com.demo.poi.xlsx.util;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class XlsxFileWriter {

    /**
     * 将 XSSFWorkbook 写入指定路径的文件
     *
     * @param workbook 要写入的 XSSFWorkbook 对象
     * @param filePath 目标文件路径（如：/path/to/file.xlsx）
     */
    public static void writeWorkbookToFile(XSSFWorkbook workbook, String filePath){
        // 检查参数是否为空
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook不能为空");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 确保文件路径以 .xlsx 结尾
        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        // 使用 try-with-resources 确保流关闭
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
            log.info("文件已成功写入: {}", filePath);
        } catch (IOException e) {
            log.error("文件写入失败: {}", e.getMessage());
        }
    }

}
