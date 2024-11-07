package com.demo.poi.entity;

import com.demo.poi.xlsx.annotations.XlsxColumn;
import lombok.Data;

@Data
public class DemoVO {
    @XlsxColumn(value = "属性1",annotations = "表头批注",combobox = {"项目1","项目2","项目3"})
    private String prop01;
    @XlsxColumn(value = "属性2",width = 3900)
    private String prop02;
    @XlsxColumn("属性3")
    private String prop03;
    @XlsxColumn("属性4")
    private String prop04;
    @XlsxColumn("属性5")
    private String prop05;
}
