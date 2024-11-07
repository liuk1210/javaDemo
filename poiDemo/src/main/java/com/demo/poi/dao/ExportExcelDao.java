package com.demo.poi.dao;

import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ExportExcelDao {
    List<JSONObject> list(String sql);
}
