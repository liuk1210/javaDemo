package com.demo.poi.controller;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.service.ExportExcelService;
import com.demo.poi.xlsx.XlsxReader;
import com.demo.poi.xlsx.XlsxSheet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Excel工具")
@RestController
@RequestMapping("/excel")
@Slf4j
@RequiredArgsConstructor
public class ExportExcelController {

    private final ExportExcelService exportExcelService;

    @PostMapping(value = "/exportBigDataBySql", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "根据sql导出查询结果")
    public void exportBigDataBySql(@RequestBody String sql, HttpServletResponse response) {
        exportExcelService.exportBigDataBySql(sql, response);
    }

    @PostMapping(value = "readXlsx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "读取xlsx内容", description = "将读取到的内容转换成json")
    public List<JSONObject> readXlsx(@RequestParam("file") MultipartFile file,
                                     @RequestParam("titleStartRow") int titleStartRow,
                                     @RequestParam("titleRowNum") int titleRowNum) {
        return XlsxReader.read(file, titleStartRow, titleRowNum);
    }

    @PostMapping(value = "readXlsxAndCheckTitleDemo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "读取xlsx内容", description = "将读取到的内容转换成json，同时校验表头")
    public List<JSONObject> readXlsxAndCheckTitleDemo(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("titleStartRow") int titleStartRow) {
        XlsxSheet arg = ExportExcelService.getSheetArg("sheet1");
        return XlsxReader.read(file, titleStartRow, arg.getTitle());
    }

    @PostMapping(value = "/demo")
    @Operation(summary = "导出模板demo")
    public void demo(HttpServletResponse response) {
        exportExcelService.exportDemo(response);
    }

}
