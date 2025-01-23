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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        title3.add( CellArg.getInstance().title().value("分类1").key("fl1").combobox(new String[]{"a","b","c"}));
        title3.add( CellArg.getInstance().title().value("分类2-1").key("fl2-1"));
        title3.add( CellArg.getInstance().title().value("分类2-2").key("fl2-2"));
        //级联第2列的值，例如第二列选了b，这列就只能从b的选项里面选
        Map<String, String[]> map = new HashMap<>();
        map.put("a", new String[]{"a1", "a2", "a3"});
        map.put("b", new String[]{"b1", "b2", "b3"});
        map.put("c", new String[]{"c1", "c2", "c3"});
        title3.add( CellArg.getInstance().title().value("其他1").key("qt1").comboboxIndirect(2,map));
        title3.add( CellArg.getInstance().title().value("其他2").key("qt2"));

        arg.addTitleRow(title1).addTitleRow(title2).addTitleRow(title3);

        List<CellArg> dataArg = new ArrayList<>();
        dataArg.add(CellArg.getInstance().value("123"));
        dataArg.add(CellArg.getInstance().value("a"));
        dataArg.add(CellArg.getInstance().value("b232"));
        dataArg.add(CellArg.getInstance().value("c233"));
        //针对具体的数据行也可以单独设置级联某列的值
        Map<String, String[]> map2 = new HashMap<>();
        map2.put("a", new String[]{"a11", "a22"});
        map2.put("b", new String[]{"b11", "b22", "b3"});
        map2.put("c", new String[]{"c11", "c22", "c3"});
        dataArg.add(CellArg.getInstance().value("").key("qt1").comboboxIndirect(2,map2));
        arg.addDataRow(dataArg);

        List<CellArg> dataArg2 = new ArrayList<>();
        dataArg2.add(CellArg.getInstance().value("1231"));
        dataArg2.add(CellArg.getInstance().value("b"));
        dataArg2.add(CellArg.getInstance().value("b2321"));
        dataArg2.add(CellArg.getInstance().value("c2331"));
        Map<String, String[]> map3 = new HashMap<>();
        map3.put("a", new String[]{"a3311", "a2233"});
        map3.put("b", new String[]{"b111", "b222", "b3233"});
        map3.put("c", new String[]{"c111", "c212", "c31"});
        dataArg2.add(CellArg.getInstance().value("").key("qt1").comboboxIndirect(2,map3));
        arg.addDataRow(dataArg2);

        return arg;
    }

    public static void main(String[] args) {
        //导出到本地文件
        SheetArg arg = getSheetArg("sheet1");
        XlsxExporter.export("demo.xlsx", arg);
    }

}
