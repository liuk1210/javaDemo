package com.demo.mybatis.dao;

import com.demo.mybatis.entity.Table;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TableDao {
    List<Table> listTableColumn(String username, String tableName);
    List<String> listAllTableName();
    List<Table> listMySQLTableColumn(String dbName, String tableName);
}
