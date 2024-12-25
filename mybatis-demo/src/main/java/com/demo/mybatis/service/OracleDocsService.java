package com.demo.mybatis.service;

import com.demo.mybatis.dao.TableDao;
import com.demo.mybatis.entity.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OracleDocsService {
    private final TableDao tableDao;
    private final Environment env;

    public String getDocs(String tableName) {
        String username = Objects.requireNonNull(env.getProperty("spring.datasource.username")).toUpperCase();
        List<Table> list = tableDao.listTableColumn(username, tableName.toUpperCase());
        StringBuilder rs = new StringBuilder();
        rs.append("### ").append(tableName.toUpperCase())
                .append("   \n").append("\n").append("**表描述：** ")
                .append(list.get(0).getTableComments())
                .append("   \n").append("\n").append("**表备注：** ")
                .append(list.get(0).getTableComments()).append("   \n");
        rs.append("| 主键 | 字段名 | 字段描述 | 数据类型 | 长度 | 可空 | 备注 |\n");
        rs.append("| ------- | ------- | ------- | ------- | ------- | ------- | ------- |\n");
        for (Table obj : list) {
            rs.append("|");
            if ("Y".equals(obj.getIsPrimaryKey())) {
                rs.append(" **✓** | **");
                rs.append(obj.getColumnName());
                rs.append("** |");
            } else {
                rs.append("  | ").append(obj.getColumnName()).append(" |");
            }
            rs.append(" ").append(obj.getComments()).append(" |");
            rs.append(" ").append(obj.getType()).append(" |");
            rs.append(" ").append(obj.getDataLength()).append(" |");
            rs.append(" ").append("Y".equals(obj.getNullable()) ? "✓" : "").append(" |");
            rs.append(" ").append(obj.getComments()).append(" |\n");
        }
        return rs.toString();
    }
}
