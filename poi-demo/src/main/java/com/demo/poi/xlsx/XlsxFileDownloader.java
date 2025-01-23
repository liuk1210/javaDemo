package com.demo.poi.xlsx;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class XlsxFileDownloader {

    //下载文件
    public static void download(String fileName, HttpServletResponse response, Workbook wb) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, String.valueOf(StandardCharsets.UTF_8)));
            wb.write(response.getOutputStream());
            if (wb instanceof SXSSFWorkbook sxssf) {
                sxssf.dispose();
            }
            wb.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
