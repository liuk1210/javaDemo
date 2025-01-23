package com.demo.poi.service;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.dao.ExportExcelDao;
import com.demo.poi.xlsx.XlsxBigDataExporter;
import com.demo.poi.xlsx.XlsxCell;
import com.demo.poi.xlsx.XlsxExporter;
import com.demo.poi.xlsx.XlsxSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        XlsxBigDataExporter.exportBigData("导出数据.xlsx", list, response);
        long now = System.currentTimeMillis();
        log.info("导出excel耗时{}ms", now - end);
    }

    public void exportDemo(HttpServletResponse response) {
        XlsxSheet arg = getSheetArg("sheet1");
        XlsxExporter.export("demo.xlsx", arg, response);
    }

    public static XlsxSheet getSheetArg(String sheetName) {
        XlsxSheet sheet = new XlsxSheet(sheetName);

        List<XlsxCell> title1 = List.of(
                XlsxCell.ofT("序号").annotations("这是注解"),
                XlsxCell.ofT("分类"),
                XlsxCell.ofT("分类"),
                XlsxCell.ofT("分类"),
                XlsxCell.ofT("其他"),
                XlsxCell.ofT("其他")
        );
        List<XlsxCell> title2 = List.of(
                XlsxCell.ofT("序号"),
                XlsxCell.ofT("分类1"),
                XlsxCell.ofT("分类2"),
                XlsxCell.ofT("分类2"),
                XlsxCell.ofT("其他1"),
                XlsxCell.ofT("其他2")
        );
        //只需要在最后一行标题行设置key，用于自动识别json数据key初始化数据区域用
        Map<String, String[]> map = new HashMap<>();
        map.put("a", new String[]{"a1", "a2", "a3"});
        map.put("b", new String[]{"b1", "b2", "b3"});
        map.put("c", new String[]{"c1", "c2", "c3"});
        List<XlsxCell> title3 = List.of(
                XlsxCell.ofT("xh", "序号"),
                XlsxCell.ofT("fl1", "分类1").combobox(new String[]{"a", "b", "c"}),
                XlsxCell.ofT("fl2-1", "分类2-1"),
                XlsxCell.ofT("fl2-2", "分类2-2"),
                //级联第2列的值，例如第二列选了b，这列就只能从b的选项里面选
                XlsxCell.ofT("qt1", "其他1").comboboxIndirect(2, map),
                XlsxCell.ofT("qt2", "其他2")
        );
        sheet.addTitleRow(title1).addTitleRow(title2).addTitleRow(title3);

        //针对具体的数据行也可以单独设置级联某列的值
        Map<String, String[]> map2 = new HashMap<>();
        map2.put("a", new String[]{"a11", "a22"});
        map2.put("b", new String[]{"b11", "b22", "b3"});
        map2.put("c", new String[]{"c11", "c22", "c3"});
        sheet.addDataRow(
                List.of(
                        XlsxCell.of("123"),
                        XlsxCell.of("a"),
                        XlsxCell.of("123"),
                        XlsxCell.of("123"),
                        XlsxCell.of("qt1", "").comboboxIndirect(2, map2)
                ));

        Map<String, String[]> map3 = new HashMap<>();
        map3.put("a", new String[]{"a3311", "a2233"});
        map3.put("b", new String[]{"b111", "b222", "b3233"});
        map3.put("c", new String[]{"c111", "c212", "c31"});
        sheet.addDataRow(
                List.of(
                        XlsxCell.of("3"),
                        XlsxCell.of("b"),
                        XlsxCell.of("3"),
                        XlsxCell.of("3"),
                        XlsxCell.of("qt1", "").comboboxIndirect(2, map3)
                ));

        JSONObject dataArg3 = new JSONObject();
        dataArg3.put("xh", "序号01");
        //...可以添加其他字段

        sheet.addDataRow(dataArg3);
        return sheet;
    }

    public static void main(String[] args) {
        //导出到本地文件
        XlsxSheet arg = getSheetArg("sheet1");
        XlsxExporter.export("demo.xlsx", arg);
    }

}
