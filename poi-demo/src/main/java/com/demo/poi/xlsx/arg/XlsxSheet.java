package com.demo.poi.xlsx.arg;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;

@Data
public class XlsxSheet {
    private String name;
    //模板行
    private List<List<XlsxCell>> template;
    //支持多行表头
    private List<List<XlsxCell>> title;
    //数据行
    private List<List<XlsxCell>> data;
    //待合并的单元格
    private List<CellRangeAddress> mergedCellRangeAddress;

    public XlsxSheet(String name) {
        this.name = name;
        this.template = new ArrayList<>();
        this.title = new ArrayList<>();
        this.data = new ArrayList<>();
        this.mergedCellRangeAddress = new ArrayList<>();
    }

    public XlsxSheet addMergedCellRangeAddress(CellRangeAddress cellAddresses){
        this.mergedCellRangeAddress.add(cellAddresses);
        return this;
    }

    //添加一行模板行
    public XlsxSheet addTemplateRow(List<XlsxCell> cells) {
        this.template.add(cells);
        return this;
    }

    //添加一行表头
    public XlsxSheet addTitleRow(List<XlsxCell> cells) {
        for(XlsxCell xlsxCell :cells){
            if(xlsxCell !=null){
                xlsxCell.title();
            }
        }
        this.title.add(cells);
        return this;
    }

    /**
     * 添加一行数据
     * @param dataObj 实体类对象
     */
    public void addDataRow(JSONObject dataObj){
        List<XlsxCell> dataList = new ArrayList<>();
        for(XlsxCell title:this.title.get(this.title.size() - 1)){
            dataList.add(
                    XlsxCell.of(dataObj.getString(title.getKey())).key(title.getKey())
            );
        }
        this.data.add(dataList);
    }

    /**
     * 添加一行数据
     * @param cells 数据行
     */
    public void addDataRow(List<XlsxCell> cells){
        this.data.add(cells);
    }

    /**
     * 根据编码获取表头单元格，取自表头最后一行
     * @param key 表头字段key
     * @return 表头单元格
     */
    public XlsxCell getLastTitleRowCellArgByKey(String key) {
        if (this.title.isEmpty() || StringUtils.isBlank(key)) {
            return null;
        } else {
            for (XlsxCell xlsxCell : this.title.get(this.title.size() - 1)) {
                if (xlsxCell != null && key.equals(xlsxCell.getKey())) {
                    return xlsxCell;
                }
            }
        }
        return null;
    }

    /**
     * 根据编码获取表头单元格，取自表头最后一行
     * @param key 表头字段key
     * @return 表头单元格
     */
    public XlsxCell getLastDataRowCellArgByKey(String key) {
        if (this.data.isEmpty() || StringUtils.isBlank(key)) {
            return null;
        } else {
            for (XlsxCell xlsxCell : this.data.get(this.data.size() - 1)) {
                if (xlsxCell != null && key.equals(xlsxCell.getKey())) {
                    return xlsxCell;
                }
            }
        }
        return null;
    }

}
