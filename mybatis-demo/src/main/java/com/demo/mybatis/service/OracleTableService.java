package com.demo.mybatis.service;

import com.demo.mybatis.dao.TableDao;
import com.demo.mybatis.entity.Table;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class OracleTableService {
    @Resource
    private TableDao tableDao;
    @Resource
    private Environment env;

    public List<String> listAllTableName() {
        return tableDao.listAllTableName();
    }

    public byte[] getTableInfo(String tableName, String packageName, String ignorePrefix) throws Exception {
        String classBaseName = getJavaClassBaseName(tableName, ignorePrefix);

        String username = Objects.requireNonNull(env.getProperty("spring.datasource.username")).toUpperCase();
        List<Table> list = tableDao.listTableColumn(username, tableName.toUpperCase());
        list.forEach(this::setTableData);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bo);

        StringBuilder mybatisXml = this.mybatisXmlFileContent(list, tableName, packageName, classBaseName);
        this.addStringBuilder2Zip(zip, mybatisXml, "mybatis/" + classBaseName + "Mapper.xml");

        StringBuilder daoFile = this.javaDaoFileContent(packageName, classBaseName);
        this.addStringBuilder2Zip(zip, daoFile, "dao/" + classBaseName + "Dao.java");

        StringBuilder entity = this.javaVOFileContent(list, packageName, classBaseName);
        this.addStringBuilder2Zip(zip, entity, "entity/" + classBaseName + ".java");

        StringBuilder entityArg = this.javaArgFileContent(list, packageName, classBaseName);
        this.addStringBuilder2Zip(zip, entityArg, "entity/arg/" + classBaseName + "Arg.java");

        StringBuilder queryArg = this.javaQueryArgFileContent(packageName, classBaseName);
        this.addStringBuilder2Zip(zip, queryArg, "entity/arg/" + classBaseName + "QueryArg.java");

        StringBuilder serviceFile = this.javaServiceFileContent(packageName, classBaseName);
        this.addStringBuilder2Zip(zip, serviceFile, "service/" + classBaseName + "Service.java");

        StringBuilder serviceImplFile = this.javaServiceImplFileContent(packageName, classBaseName);
        this.addStringBuilder2Zip(zip, serviceImplFile, "service/impl/" + classBaseName + "ServiceImpl.java");

        StringBuilder controllerFile = this.javaControllerFileContent(packageName, classBaseName);
        this.addStringBuilder2Zip(zip, controllerFile, "controller/" + classBaseName + "Controller.java");

        StringBuilder setFile = this.javaSetterFileContent(list, classBaseName);
        this.addStringBuilder2Zip(zip, setFile,  classBaseName + "Setter.java");

        zip.finish();
        return bo.toByteArray();
    }

    private StringBuilder javaQueryArgFileContent(String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.entity.arg;
                
                import io.swagger.annotations.ApiModel;
                import io.swagger.annotations.ApiModelProperty;
                import lombok.Data;
                
                import javax.validation.constraints.Min;
                import javax.validation.constraints.Pattern;
                import javax.validation.constraints.Size;
                
                @Data
                @ApiModel("分页查询参数")
                public class %sQueryArg {
                
                    @Min(value = 0,message = "pageSize不能小于0")
                    private Integer pageSize;
               
                    @Min(value = 0,message = "pageIndex不能小于0")
                    private Integer pageIndex;
               
                }
                """.formatted(packageName,classBaseName));
        return content;
    }

    private StringBuilder javaSetterFileContent(List<Table> list, String classBaseName) {
        StringBuilder content = new StringBuilder();
        String classBaseNameLow = classBaseName.substring(0, 1).toLowerCase() + classBaseName.substring(1);
        for(Table table:list){
            String upColumnName = table.getProperty().substring(0,1).toUpperCase() + table.getProperty().substring(1);
            content.append(classBaseNameLow).append(".set").append(upColumnName).append("(arg.get").append(upColumnName).append("());\t//").append(table.getComments()).append("\n");
        }
        return content;
    }

    private String getJavaClassBaseName(String tableName, String ignorePrefix) {
        String className;
        if (StringUtils.isBlank(ignorePrefix)) {
            className = lineToHump(tableName);
        } else {
            className = lineToHump(tableName.toUpperCase().replaceFirst(ignorePrefix.toUpperCase(), ""));
        }
        return className.substring(0, 1).toUpperCase() + className.substring(1);
    }

    private StringBuilder javaControllerFileContent(String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        String classBaseNameLow = classBaseName.substring(0, 1).toLowerCase() + classBaseName.substring(1);
        content.append("""
                package %s.controller;
                
                import %s.entity.arg.%sArg;
                import %s.service.%sService;
                import pub.annotations.SwaggerApi;
                import io.swagger.annotations.Api;
                import io.swagger.annotations.ApiOperation;
                import lombok.RequiredArgsConstructor;
                import org.apache.ibatis.annotations.Param;
                import org.springframework.web.bind.annotation.*;
                import org.springframework.web.multipart.MultipartFile;
                
                import java.util.List;
                import java.util.Map;
                
                @SwaggerApi
                @Api(tags = "")
                @RestController
                @RequestMapping("/%s")
                @RequiredArgsConstructor
                public class %sController {
                
                    private final %sService %sService;
                
                }
                
                """.formatted(packageName, packageName, classBaseName, packageName, classBaseName,
                packageName.substring(packageName.lastIndexOf(".")+1), classBaseName, classBaseName, classBaseNameLow));
        return content;
    }

    private StringBuilder javaDaoFileContent(String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.dao;
                
                import %s.entity.%s;
                import %s.entity.arg.%sQueryArg;
                import org.apache.ibatis.annotations.Param;
                import org.springframework.stereotype.Repository;
                
                import java.util.List;
                
                @Repository
                public interface %sDao {
                    void insertAll(@Param("list") List<%s> list);
                
                    void insert(%s vo);
                
                    void update(%s vo);
                
                    %s findById(String id);
                
                    List<%s> pageList(%sQueryArg arg);
                
                    Integer count(%sQueryArg arg);
                }
                
                """.formatted(packageName, packageName, classBaseName,packageName, classBaseName,
                classBaseName, classBaseName, classBaseName, classBaseName
                ,classBaseName,classBaseName,classBaseName,classBaseName));
        return content;
    }

    private StringBuilder javaServiceFileContent(String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.service;
                
                public interface %sService {
                
                }
                
                """.formatted(packageName, classBaseName));
        return content;
    }

    private StringBuilder javaServiceImplFileContent(String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        String classBaseNameLow = classBaseName.substring(0, 1).toLowerCase() + classBaseName.substring(1);
        content.append("""
                package %s.service.impl;
                
                import %s.dao.%sDao;
                import %s.service.%sService;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.stereotype.Service;
                
                @Service
                @Slf4j
                @RequiredArgsConstructor
                public class %sServiceImpl implements %sService {
                    private final %sDao %sDao;
                
                }
                
                """.formatted(packageName, packageName, classBaseName,packageName,classBaseName, classBaseName, classBaseName, classBaseName, classBaseNameLow));
        return content;
    }

    private StringBuilder javaArgFileContent(List<Table> list, String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("package ").append(packageName).append(".entity.arg;\n\n");
        content.append("""
                import io.swagger.annotations.ApiModelProperty;
                import lombok.Data;
                
                import javax.persistence.EnumType;
                import javax.persistence.Enumerated;
                import javax.validation.constraints.NotBlank;
                import javax.validation.constraints.Size;
                import java.sql.Timestamp;
                
                @Data
                """);
        content.append("public class ").append(classBaseName).append("Arg {\n\n");
        content.append(this.javaVO(list));
        content.append("}\n");
        return content;
    }

    private StringBuilder javaVOFileContent(List<Table> list, String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("package ").append(packageName).append(".entity;\n\n");
        content.append("""
                import io.swagger.annotations.ApiModelProperty;
                import lombok.Data;
                
                import javax.persistence.EnumType;
                import javax.persistence.Enumerated;
                import javax.validation.constraints.NotBlank;
                import javax.validation.constraints.Size;
                import java.sql.Timestamp;
                
                @Data
                """);
        content.append("public class ").append(classBaseName).append(" {\n");
        content.append(this.javaVONotValidate(list));
        content.append("}\n");
        return content;
    }

    private StringBuilder mybatisXmlFileContent(List<Table> list, String tableName, String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="%s.dao.%sDao">
                
                """.formatted(packageName,classBaseName));
        content.append("<resultMap id=\"resultMap\" type=\"").append(packageName).append(".entity.").append(classBaseName).append("\">\n");
        content.append(this.mybatisResultMap(list));
        content.append("</resultMap>\n");
        content.append("<insert id=\"insertAll\">\n");
        content.append(this.insertBatchSql(list, tableName));
        content.append("</insert>\n");
        content.append("<insert id=\"insert\">\n");
        content.append(this.insertSql(list, tableName));
        content.append("</insert>\n");
        content.append("<update id=\"update\">\n");
        content.append(this.updateSql(list, tableName));
        content.append("</update>\n\n");

        content.append("""
                    <select id="findById" resultMap="resultMap">
                            select * from %s where id_= #{id}
                    </select>
                
                """.formatted(tableName));

        content.append("""
                    <select id="pageList" resultMap="resultMap">
                        SELECT *
                        FROM (SELECT tt.*, ROWNUM AS rowno
                        FROM (
                        select * from %s
                        <include refid="querySql"></include>
                        ORDER BY CREATE_TIME_ DESC
                        ) tt
                        WHERE ROWNUM &lt;= ((#{pageIndex} + '1') * #{pageSize})) tb
                        WHERE tb.rowno > (#{pageIndex} * #{pageSize})
                    </select>
                
                    <select id="count" resultType="java.lang.Integer">
                        select count(1) from %s
                        <include refid="querySql"></include>
                    </select>
                
                    <sql id="querySql">
                
                    </sql>
                """.formatted(tableName,tableName));
        content.append("</mapper>");
        return content;
    }

    private void addStringBuilder2Zip(ZipOutputStream zip, StringBuilder content, String fileName) throws Exception {
        zip.putNextEntry(new ZipEntry(fileName));
        zip.write(content.toString().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String javaVO(List<Table> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table t : list) {
            if ("N".equals(t.getNullable())) {
                stringBuilder.append("\t@NotBlank(message = \"[").append(t.getComments()).append("]不能为空\")\n");
            }
            if ("String".equals(t.getJavaType())) {
                stringBuilder.append("\t@Size(max = ").append(t.getDataLength()).append(", message = \"[").append(t.getComments()).append("]长度不能超过").append(t.getDataLength()).append("个字符\")\n");
            }
            stringBuilder.append("\t@ApiModelProperty(\"").append(t.getComments()).append("\")\n")
                    .append("\tprivate ")
                    .append(t.getJavaType())
                    .append(" ")
                    .append(t.getProperty())
                    .append(";\n\n");
        }
        return stringBuilder.toString();
    }

    private String javaVONotValidate(List<Table> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table t : list) {
            stringBuilder.append("\t@ApiModelProperty(\"").append(t.getComments()).append("\")\n")
                    .append("\tprivate ")
                    .append(t.getJavaType())
                    .append(" ")
                    .append(t.getProperty())
                    .append(";\n\n");
        }
        return stringBuilder.toString();
    }

    private String insertBatchSql(List<Table> list, String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tINSERT ALL\n");
        stringBuilder.append("\t<foreach collection=\"list\" item=\"item\">\n");
        stringBuilder.append("\t\tINTO ").append(tableName).append(" (\n");
        for (int i = 0; i < list.size(); i++) {
            Table t = list.get(i);
            if (i == list.size() - 1) {
                //stringBuilder.append(t.getColumnName()).append(")").append("\n");
                stringBuilder.append("\t\t\t").append(t.getColumnName()).append(")\t--").append(t.getComments()).append("\n");
            } else {
                //stringBuilder.append(t.getColumnName()).append(",").append("\n");
                stringBuilder.append("\t\t\t").append(t.getColumnName()).append(",\t--").append(t.getComments()).append("\n");
            }
        }
        stringBuilder.append("\t\tVALUES(\n");
        for (int i = 0; i < list.size(); i++) {
            Table t = list.get(i);
            if (i == list.size() - 1) {
                //stringBuilder.append("#{item.").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("})").append("\n");
                stringBuilder.append("\t\t\t" + "#{item.").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("})\t--").append(t.getComments()).append("\n");
            } else {
                //stringBuilder.append("#{item.").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("},").append("\n");
                stringBuilder.append("\t\t\t" + "#{item.").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("},\t--").append(t.getComments()).append("\n");
            }
        }
        stringBuilder.append("""
                \t</foreach>
                \tSELECT 1 FROM dual
                """);
        return stringBuilder.toString();
    }

    //UPDATE语句
    private String updateSql(List<Table> list, String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tUPDATE ").append(tableName).append(" SET \n");
        for (int i = 0; i < list.size(); i++) {
            Table t = list.get(i);
            if ("ID".equalsIgnoreCase(t.getColumnName().replaceAll("_", ""))) {
                continue;
            }
            if (i > 0 && i == list.size() - 1) {
                stringBuilder.append("\t\t").append(t.getColumnName()).append("=#{").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("}\t--").append(t.getComments()).append("\n");
            } else {
                stringBuilder.append("\t\t").append(t.getColumnName()).append("=#{").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("},\t--").append(t.getComments()).append("\n");
            }
        }
        stringBuilder.append("\tWHERE ID_=#{id,jdbcType=VARCHAR}\n");
        return stringBuilder.toString();
    }

    //INSERT语句
    private String insertSql(List<Table> list, String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tINSERT INTO ").append(tableName).append(" (\n");
        for (int i = 0; i < list.size(); i++) {
            Table t = list.get(i);
            if (i == list.size() - 1) {
                stringBuilder.append("\t\t").append(t.getColumnName()).append(")\t--").append(t.getComments()).append("\n");
            } else {
                stringBuilder.append("\t\t").append(t.getColumnName()).append(",\t--").append(t.getComments()).append("\n");
            }
        }
        stringBuilder.append("\tVALUES(\n");
        for (int i = 0; i < list.size(); i++) {
            Table t = list.get(i);
            if (i == list.size() - 1) {
                stringBuilder.append("\t\t" + "#{").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("})\t--").append(t.getComments()).append("\n");
            } else {
                stringBuilder.append("\t\t" + "#{").append(t.getProperty()).append(",jdbcType=").append(t.getJdbcType()).append("},\t--").append(t.getComments()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private String mybatisResultMap(List<Table> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table t : list) {
            if ("ID_".equals(t.getColumnName()) || "ID".equals(t.getColumnName())) {
                stringBuilder.append("\t<id column=\"")
                        .append(t.getColumnName())
                        .append("\" jdbcType=\"")
                        .append(t.getJdbcType())
                        .append("\" property=\"")
                        .append(t.getProperty())
                        .append("\"/>\n");
            } else {
                stringBuilder.append("\t<result column=\"")
                        .append(t.getColumnName())
                        .append("\" jdbcType=\"")
                        .append(t.getJdbcType())
                        .append("\" property=\"")
                        .append(t.getProperty())
                        .append("\"/>\n");
            }
        }
        return stringBuilder.toString();
    }

    private void setTableData(Table table) {
        table.setProperty(lineToHump(table.getColumnName().replaceAll("^F_", ""))
                .replaceAll("_", ""));
        String jdbcType = "";
        String javaType = "";
        switch (table.getType()) {
            case "VARCHAR2":
                jdbcType = "VARCHAR";
                javaType = "String";
                break;
            case "CHAR":
                jdbcType = "CHAR";
                javaType = "String";
                break;
            case "TIMESTAMP":
            case "DATE":
                jdbcType = "TIMESTAMP";
                javaType = "Timestamp";
                break;
            case "NUMBER":
                jdbcType = "DOUBLE";
                javaType = "Double";
                break;
            case "INTEGER":
                jdbcType = "INTEGER";
                javaType = "Integer";
                break;
            case "CLOB":
                jdbcType = "CLOB";
                javaType = "String";
            default:
                break;
        }
        table.setJdbcType(jdbcType);
        table.setJavaType(javaType);
    }

    /**
     * 下划线转驼峰
     */
    private String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = Pattern.compile("_(\\w)").matcher(str);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
