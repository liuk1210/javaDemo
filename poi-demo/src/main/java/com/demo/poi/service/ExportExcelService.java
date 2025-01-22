package com.demo.poi.service;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.dao.ExportExcelDao;
import com.demo.poi.xlsx.arg.CellArg;
import com.demo.poi.xlsx.arg.SheetArg;
import com.demo.poi.xlsx.util.XlsxExporter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExportExcelService {
    private final ExportExcelDao exportExcelDao;

    public void exportBigDataBySql(String sql, HttpServletResponse response) {
        long start = System.currentTimeMillis();
        List<JSONObject> list = exportExcelDao.list(sql);
        long end = System.currentTimeMillis();
        log.info("数据库查询耗时：{}ms", end - start);
        XlsxExporter.exportBigData("导出数据.xlsx", list, response);
        long now = System.currentTimeMillis();
        log.info("导出excel耗时{}ms", now - end);
    }

    public void exportDemo(HttpServletResponse response) {
        SheetArg arg = getSheetArg("sheet1");
        XlsxExporter.export("demo.xlsx", arg, response);
    }

    public static SheetArg getSheetArg(String sheetName) {
        SheetArg arg = new SheetArg();
        arg.setName(sheetName);
        List<CellArg> title1 = new ArrayList<>();
        title1.add( CellArg.getInstance().title().value("序号"));
        title1.add( CellArg.getInstance().title().value("分类"));
        title1.add( CellArg.getInstance().title().value("分类"));
        title1.add( CellArg.getInstance().title().value("分类"));
        title1.add( CellArg.getInstance().title().value("其他"));
        title1.add( CellArg.getInstance().title().value("其他"));

        List<CellArg> title2 = new ArrayList<>();
        title2.add( CellArg.getInstance().title().value("序号"));
        title2.add( CellArg.getInstance().title().value("分类1"));
        title2.add( CellArg.getInstance().title().value("分类2"));
        title2.add( CellArg.getInstance().title().value("分类2"));
        title2.add( CellArg.getInstance().title().value("其他1"));
        title2.add( CellArg.getInstance().title().value("其他2"));

        List<CellArg> title3 = new ArrayList<>();
        title3.add( CellArg.getInstance().title().value("序号").key("xh"));
        title3.add( CellArg.getInstance().title().value("分类1").key("fl1"));
        title3.add( CellArg.getInstance().title().value("分类2-1").key("fl2-1"));
        title3.add( CellArg.getInstance().title().value("分类2-2").key("fl2-2"));
        title3.add( CellArg.getInstance().title().value("其他1").key("qt1"));
        title3.add( CellArg.getInstance().title().value("其他2").key("qt2"));

        arg.addTitleRow(title1).addTitleRow(title2).addTitleRow(title3);

        return arg;
    }

    public static void main(String[] args) {
        //导出到本地文件
        SheetArg arg = getSheetArg("sheet1");
        XlsxExporter.export("demo.xlsx", arg);
    }

}
