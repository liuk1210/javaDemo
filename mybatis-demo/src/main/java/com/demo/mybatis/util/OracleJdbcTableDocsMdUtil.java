package com.demo.mybatis.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OracleJdbcTableDocsMdUtil {

    private static final String DB_URL = "jdbc:oracle:thin:@ip:1521:orcl";
    private static final String DB_USER = "xxx";
    private static final String DB_PASSWORD = "xxx";

    public static void main(String[] args) {
        System.out.println(buildMdStr("tb"));
    }

    public static String buildMdStr(String tableName){
        String sql = """
                SELECT CASE WHEN D.COLUMN_NAME IS NULL THEN 'N' ELSE 'Y' END
                                  AS IS_PRIMARY_KEY,
                       B.COLUMN_NAME,
                       A.COMMENTS,
                       B.DATA_TYPE,
                       B.DATA_LENGTH,
                       B.NULLABLE,
                       C.COMMENTS AS TABLE_COMMENTS
                FROM ALL_COL_COMMENTS A
                         JOIN ALL_TAB_COLUMNS B ON A.OWNER = B.OWNER AND A.TABLE_NAME = B.TABLE_NAME AND A.COLUMN_NAME = B.COLUMN_NAME
                         JOIN DBA_TAB_COMMENTS C ON A.OWNER = C.OWNER AND A.TABLE_NAME = C.TABLE_NAME
                         LEFT JOIN (SELECT COLS.COLUMN_NAME, CONS.OWNER, CONS.TABLE_NAME
                                    FROM ALL_CONSTRAINTS CONS
                                             JOIN ALL_CONS_COLUMNS COLS
                                                  ON CONS.CONSTRAINT_NAME = COLS.CONSTRAINT_NAME
                                    WHERE CONS.CONSTRAINT_TYPE = 'P') D
                                   ON A.OWNER = D.OWNER AND A.TABLE_NAME = D.TABLE_NAME AND D.COLUMN_NAME = A.COLUMN_NAME
                WHERE A.OWNER = ?
                  AND A.TABLE_NAME = ?
                ORDER BY B.COLUMN_ID
                """;
        List<Object> params = new ArrayList<>();
        params.add(DB_USER.toUpperCase());
        params.add(tableName.toUpperCase());
        List<JSONObject> list = queryOracleDatabaseWithParamsAndConvertToJson(sql, params);
        StringBuilder rs = new StringBuilder();
        rs.append("### ").append(tableName.toUpperCase())
                .append("   \n").append("\n").append("**表描述：** ")
                .append(list.get(0).getString("TABLE_COMMENTS"))
                .append("   \n").append("\n").append("**表备注：** ")
                .append(list.get(0).getString("TABLE_COMMENTS")).append("   \n");
        rs.append("| 主键 | 字段名 | 字段描述 | 数据类型 | 长度 | 可空 | 备注 |\n");
        rs.append("| ------- | ------- | ------- | ------- | ------- | ------- | ------- |\n");
        for (JSONObject obj : list) {
            rs.append("|");
            if ("Y".equals(obj.getString("IS_PRIMARY_KEY"))) {
                rs.append(" **✓** | **");
                rs.append(obj.getString("COLUMN_NAME"));
                rs.append("** |");
            } else {
                rs.append("  | ").append(obj.getString("COLUMN_NAME")).append(" |");
            }
            rs.append(" ").append(obj.getString("COMMENTS")).append(" |");
            rs.append(" ").append(obj.getString("DATA_TYPE")).append(" |");
            rs.append(" ").append(obj.getString("DATA_LENGTH")).append(" |");
            rs.append(" ").append("Y".equals(obj.getString("NULLABLE")) ? "✓" : "").append(" |");
            rs.append(" ").append(obj.getString("COMMENTS")).append(" |\n");
        }
        return rs.toString();
    }

    public static List<JSONObject> queryOracleDatabaseWithParamsAndConvertToJson(String sql, List<Object> params) {
        List<JSONObject> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // 1. 加载Oracle数据库驱动（对于Java 8及以上版本，很多时候可省略，依赖自动加载机制）
            Class.forName("oracle.jdbc.OracleDriver");
            // 2. 建立数据库连接
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // 3. 创建PreparedStatement对象，用于执行带参数的SQL语句
            preparedStatement = connection.prepareStatement(sql);
            // 4. 设置SQL语句中的参数，遍历参数列表进行设置
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            // 5. 执行查询语句，获取结果集
            resultSet = preparedStatement.executeQuery();
            // 6. 获取结果集的元数据，用于获取列名等信息
            java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 7. 遍历结果集，将每一行数据转换为JSONObject并添加到JSONArray中
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    jsonObject.put(columnName, value);
                }
                list.add(jsonObject);
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 8. 关闭资源，释放连接、语句和结果集对象，按照先开后关的顺序逆序关闭
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        return list;
    }

}
