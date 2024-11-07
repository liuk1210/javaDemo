package com.demo.poi.xlsx.arg;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.xlsx.annotations.XlsxColumn;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Data
public class SheetArg {
    private String name;
    //模板行
    private List<List<CellArg>> template;
    //支持多行表头
    private List<List<CellArg>> title;
    //数据行
    private List<List<CellArg>> data;
    //待合并的单元格
    private List<CellRangeAddress> mergedCellRangeAddress;

    public SheetArg() {
        this.template = new ArrayList<>();
        this.title = new ArrayList<>();
        this.data = new ArrayList<>();
        this.mergedCellRangeAddress = new ArrayList<>();
    }

    public SheetArg addMergedCellRangeAddress(CellRangeAddress cellAddresses){
        this.mergedCellRangeAddress.add(cellAddresses);
        return this;
    }

    //添加一行模板行
    public SheetArg addTemplateRow(List<CellArg> cells) {
        this.template.add(cells);
        return this;
    }

    //添加一行表头
    public SheetArg addTitleRow(List<CellArg> cells) {
        for(CellArg cellArg:cells){
            if(cellArg!=null){
                cellArg.title();
            }
        }
        this.title.add(cells);
        return this;
    }

    public SheetArg initTitleRow(Class<?> clazz) {
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            List<CellArg> titleRow = new ArrayList<>();
            for (Field field : fields) {
                XlsxColumn column = field.getAnnotation(XlsxColumn.class);
                if (column == null) {
                    continue;
                }
                titleRow.add(CellArg.getInstance()
                        .columnWidth(column.width())
                        .title()
                        .key(field.getName())
                        .annotations(column.annotations())
                        .value(column.value())
                        .combobox(column.combobox()));
            }
            this.title.add(titleRow);
        }
        return this;
    }

    /**
     * 添加一行数据
     * @param dataObj 实体类对象
     * @return sheetArg
     */
    public SheetArg addDataRow(JSONObject dataObj){
        List<CellArg> dataList = new ArrayList<>();
        for(CellArg title:this.title.get(this.title.size() - 1)){
            dataList.add(CellArg.getInstance()
                    .key(title.getKey())
                    .value(dataObj.getString(title.getKey())));
        }
        this.data.add(dataList);
        return this;
    }

    /**
     * 添加一行数据
     * @param cells 数据行
     * @return sheetArg sheet参数
     */
    public SheetArg addDataRow(List<CellArg> cells){
        this.data.add(cells);
        return this;
    }

    /**
     * 根据编码获取表头单元格，取自表头最后一行
     * @param key 表头字段key
     * @return 表头单元格
     */
    public CellArg getLastTitleRowCellArgByKey(String key) {
        if (this.title.isEmpty() || StringUtils.isBlank(key)) {
            return null;
        } else {
            for (CellArg cellArg : this.title.get(this.title.size() - 1)) {
                if (cellArg != null && key.equals(cellArg.getKey())) {
                    return cellArg;
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
    public CellArg getLastDataRowCellArgByKey(String key) {
        if (this.data.isEmpty() || StringUtils.isBlank(key)) {
            return null;
        } else {
            for (CellArg cellArg : this.data.get(this.data.size() - 1)) {
                if (cellArg != null && key.equals(cellArg.getKey())) {
                    return cellArg;
                }
            }
        }
        return null;
    }

}
