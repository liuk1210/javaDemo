package com.demo.poi.service;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.dao.ExportExcelDao;
import com.demo.poi.xlsx.arg.CellArg;
import com.demo.poi.xlsx.arg.SheetArg;
import com.demo.poi.xlsx.util.XlsxUtil;
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
        XlsxUtil.exportBigData("导出数据.xlsx", list, response);
        long now = System.currentTimeMillis();
        log.info("导出excel耗时{}ms", now - end);
    }

    public void exportDemo(HttpServletResponse response) {
        SheetArg arg = new SheetArg();
        arg.setName("sheet1");
        List<CellArg> title = new ArrayList<>();
        title.add(CellArg.getInstance().title().key("k1").value("考核年度")
                .combobox(new String[]{"年度","月度","季度"}));
        Map<String, String[]> map = new HashMap<>();
        map.put("年度",new String[]{"2023年","2024年"});
        map.put("月度",new String[]{"1月","2月"});
        map.put("季度",new String[]{"1-3月","1-6月","1-9月","1-12月"});
        title.add(CellArg.getInstance().title().key("k2").value("考核期间")
                .comboboxIndirect(1,map,"_khqj")
        );
        title.add(CellArg.getInstance().title().key("k3").value("列3")
                .combobox(new String[]{"个人绩效","团队绩效"}));
        arg.addTitleRow(title);
        XlsxUtil.export("demo.xlsx", arg, response);
    }

}
