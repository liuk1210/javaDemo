package com.demo.mybatis.entity;

import lombok.Data;

@Data
public class Table {
    private String columnName;  //数据库字段
    private String type;        //数据库类型
    private String comments;    //数据库注释
    private String dataLength;  //长度
    private String nullable;    //可否为null
    private String property;    //驼峰命名
    private String jdbcType;    //jdbc类型
    private String javaType;    //java类型
}
