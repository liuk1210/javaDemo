package com.demo.poi.service;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.dao.ExportExcelDao;
import com.demo.poi.xlsx.util.XlsxUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        XlsxUtil.exportBigData("导出数据.xlsx",list,response);
        long now = System.currentTimeMillis();
        log.info("导出excel耗时{}ms", now - end);
    }

}
