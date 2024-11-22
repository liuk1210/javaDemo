package com.demo.mybatis.service;

import com.demo.mybatis.dao.TableDao;
import com.demo.mybatis.entity.Table;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MysqlTableService {

    private final TableDao tableDao;
    private final Environment env;

    public byte[] getTableInfo(String tableName, String packageName, String moduleName) throws Exception {
        String classBaseName = getJavaClassBaseName(tableName);

        String url = Objects.requireNonNull(env.getProperty("spring.datasource.url")).toUpperCase();
        String dbName = getDbNameFromUrl(url);
        List<Table> list = tableDao.listMySQLTableColumn(dbName, tableName.toUpperCase());
        list.forEach(this::setTableRowData);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bo);

        StringBuilder mybatisXml = this.mybatisXmlFileContent(list, moduleName, packageName, classBaseName);
        this.addStringBuilder2Zip(zip, mybatisXml, "mapper/" + moduleName + "/" + classBaseName + "Mapper.xml");

        StringBuilder entity = this.javaVOFileContent(list, packageName, classBaseName, moduleName);
        this.addStringBuilder2Zip(zip, entity, "model/" + moduleName + "/" + classBaseName + ".java");

        StringBuilder daoFile = this.javaDaoFileContent(packageName, classBaseName, moduleName);
        this.addStringBuilder2Zip(zip, daoFile, "dao/" + moduleName + "/" + classBaseName + "Mapper.java");

        StringBuilder serviceFile = this.javaServiceFileContent(packageName, classBaseName, moduleName);
        this.addStringBuilder2Zip(zip, serviceFile, "service/" + moduleName + "/" + classBaseName + "Service.java");

        StringBuilder serviceImplFile = this.javaServiceImplFileContent(packageName, classBaseName, moduleName);
        this.addStringBuilder2Zip(zip, serviceImplFile, "service/" + moduleName + "/impl/" + classBaseName + "ServiceImpl.java");

        StringBuilder controllerFile = this.javaControllerFileContent(packageName, classBaseName, moduleName);
        this.addStringBuilder2Zip(zip, controllerFile, "controller/" + moduleName + "/" + classBaseName + "Controller.java");

        zip.finish();
        return bo.toByteArray();
    }

    private StringBuilder javaControllerFileContent(String packageName, String classBaseName, String moduleName) {
        StringBuilder content = new StringBuilder();
        String classBaseNameLow = classBaseName.substring(0, 1).toLowerCase() + classBaseName.substring(1);
        content.append("""
                package %s.controller.%s;
                
                import %s.controller.BaseController;
                import %s.service.%s.%sService;
                import io.swagger.v3.oas.annotations.tags.Tag;
                import lombok.RequiredArgsConstructor;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RestController;
                
                @Tag(name = "XXX接口")
                @RestController
                @RequiredArgsConstructor
                @RequestMapping("/api/%s")
                public class %sController extends BaseController {
                    private final %sService %sService;
                }
                
                """.formatted(packageName, moduleName,
                packageName,
                packageName,moduleName,classBaseName,
                moduleName,
                classBaseName,
                classBaseName, classBaseNameLow));
        return content;
    }

    private StringBuilder javaServiceFileContent(String packageName, String classBaseName, String moduleName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.service.%s;
                
                import com.baomidou.mybatisplus.extension.service.IService;
                import %s.model.%s.%s;
                
                public interface %sService extends IService<%s> {
                
                }
                """.formatted(packageName, moduleName, packageName, moduleName, classBaseName, classBaseName, classBaseName));
        return content;
    }

    private StringBuilder javaServiceImplFileContent(String packageName, String classBaseName, String moduleName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.service.%s.impl;
                
                import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
                import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
                import %s.dao.%s.%sMapper;
                import %s.model.%s.%s;
                import %s.service.%s.%sService;
                import org.springframework.stereotype.Service;
                
                @Service
                public class %sServiceImpl extends ServiceImpl<%sMapper, %s> implements %sService {
                
                }
                """.formatted(packageName, moduleName,
                packageName, moduleName, classBaseName,
                packageName, moduleName, classBaseName,
                packageName, moduleName, classBaseName,
                classBaseName, classBaseName, classBaseName, classBaseName
        ));
        return content;
    }


    private StringBuilder javaDaoFileContent(String packageName, String classBaseName, String moduleName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                package %s.dao.%s;
                
                import com.baomidou.mybatisplus.core.mapper.BaseMapper;
                import %s.model.%s.%s;
                
                public interface %sMapper extends BaseMapper<%s> {
                
                }
                
                """.formatted(packageName, moduleName,
                packageName, moduleName, classBaseName,
                classBaseName, classBaseName));
        return content;
    }

    private StringBuilder mybatisXmlFileContent(List<Table> list, String moduleName, String packageName, String classBaseName) {
        StringBuilder content = new StringBuilder();
        content.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="%s.dao.%s.%sDao">
                
                """.formatted(packageName, moduleName, classBaseName));
        content.append("<resultMap id=\"resultMap\" type=\"").append(packageName).append(".model.")
                .append(moduleName).append(".").append(classBaseName).append("\">\n");
        content.append(this.mybatisResultMap(list));
        content.append("</resultMap>\n");
        content.append("</mapper>");
        return content;
    }

    private StringBuilder javaVOFileContent(List<Table> list, String packageName, String classBaseName, String moduleName) {
        StringBuilder content = new StringBuilder();
        content.append("package ").append(packageName).append(".model.").append(moduleName).append(";\n\n");
        content.append("""
                import io.swagger.v3.oas.annotations.media.Schema;
                import lombok.Data;
                
                import javax.validation.constraints.NotBlank;
                import javax.validation.constraints.Size;
                import java.sql.Timestamp;
                
                @Data
                """);
        content.append("public class ").append(classBaseName).append(" {\n");
        content.append(this.javaVO(list));
        content.append("}\n");
        return content;
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
            stringBuilder.append("\t@Schema(title =\"").append(t.getComments()).append("\")\n")
                    .append("\tprivate ")
                    .append(t.getJavaType())
                    .append(" ")
                    .append(t.getProperty())
                    .append(";\n\n");
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

    private String getDbNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL不能为空");
        }

        int lastSlashIndex = url.lastIndexOf('/');
        int questionMarkIndex = url.indexOf('?', lastSlashIndex);

        int endIndex = (questionMarkIndex == -1) ? url.length() : questionMarkIndex;

        return url.substring(lastSlashIndex + 1, endIndex);
    }

    private void addStringBuilder2Zip(ZipOutputStream zip, StringBuilder content, String fileName) throws Exception {
        zip.putNextEntry(new ZipEntry(fileName));
        zip.write(content.toString().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private void setTableRowData(Table table) {
        table.setProperty(lineToHump(table.getColumnName())
                .replaceAll("_", ""));
        String type = table.getType();
        if (StringUtils.isBlank(type)) {
            table.setJdbcType("");
            table.setJavaType("");
            return;
        }
        if (type.toUpperCase().startsWith("VARCHAR")) {
            table.setJdbcType("VARCHAR");
            table.setJavaType("String");
            return;
        }

        if (type.toUpperCase().startsWith("CHAR")) {
            table.setJdbcType("CHAR");
            table.setJavaType("String");
            return;
        }

        if (type.toUpperCase().startsWith("DATETIME")) {
            table.setJdbcType("TIMESTAMP");
            table.setJavaType("Timestamp");
            return;
        }

        if (type.toUpperCase().startsWith("TIMESTAMP")) {
            table.setJdbcType("TIMESTAMP");
            table.setJavaType("Timestamp");
            return;
        }

        if (type.toUpperCase().startsWith("DATE")) {
            table.setJdbcType("DATE");
            table.setJavaType("Date");
            return;
        }

        if (type.toUpperCase().startsWith("TINYINT")) {
            table.setJdbcType("TINYINT");
            table.setJavaType("Integer");
            return;
        }

        if (type.toUpperCase().startsWith("INT")) {
            table.setJdbcType("INTEGER");
            table.setJavaType("Integer");
            return;
        }

        if (type.toUpperCase().startsWith("DOUBLE")) {
            table.setJdbcType("DOUBLE");
            table.setJavaType("Double");
            return;
        }

        if(type.toUpperCase().contains("TEXT")) {
            table.setJdbcType("VARCHAR");
            table.setJavaType("String");
        }

    }

    private String getJavaClassBaseName(String tableName) {
        String className = lineToHump(tableName);
        return className.substring(0, 1).toUpperCase() + className.substring(1);
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
