package com.demo.poi.xlsx;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class XlsxFileWriter {

    /**
     * 将 Workbook 写入指定路径的文件
     *
     * @param wb 要写入的 Workbook 对象
     * @param filePath 目标文件路径（如：/path/to/file.xlsx）
     */
    public static void writeWorkbookToFile(Workbook wb, String filePath) {
        // 检查参数是否为空
        if (wb == null) {
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
            wb.write(outputStream);
            log.info("文件已成功写入: {}", filePath);
        } catch (IOException e) {
            log.error("文件写入失败: {}", e.getMessage());
        } finally {
            try {
                if (wb instanceof SXSSFWorkbook) {
                    SXSSFWorkbook workbook = (SXSSFWorkbook) wb;
                    workbook.dispose();
                }
                wb.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
