package com.demo.poi.xlsx.arg;

import com.demo.poi.xlsx.util.XlsxCellStyle;
import com.demo.poi.xlsx.util.XlsxExporter;
import lombok.Getter;

import java.util.Map;

@Getter
public class CellArg {

    /**
     * 单元格所在列对应表头字段名
     */
    private String key;

    /**
     * 单元格内容，字符串
     */
    private String value;

    /**
     * 单元格批注内容
     */
    private String annotations;

    /**
     * 单元格宽度，仅设置第一行有效
     */
    private int columnWidth;

    /**
     * 行高，仅设置第一列有效
     */
    private short rowHeight;

    /**
     * 单元格样式
     */
    private String style;

    /**
     * 单元格所在列-表头字段类型，用于导出时设置单元格数据类型
     */
    private String type;

    /**
     * 级联下拉框参数
     * 级联哪一列，index从1开始
     */
    private int cascadeColIndex;

    /**
     * 下拉列表可选项
     */
    private String[] comboboxOptions;

    /**
     * 级联下拉框参数
     * 单元格下拉框对应的子选项
     */
    private Map<String, String[]> comboboxSubOptionMap;

    /**
     * 是否自动换行
     */
    private boolean wrapText;

    private CellArg(){

    }

    public static CellArg getInstance() {
        CellArg instance = new CellArg();
        instance.columnWidth = 3000;
        instance.type = XlsxExporter.TYPE_STRING;
        instance.style = XlsxCellStyle.STYLE_NORMAL;
        instance.wrapText = false;
        return instance;
    }

    public CellArg readonly() {
        this.style = XlsxCellStyle.STYLE_READONLY;
        return this;
    }

    public CellArg error() {
        this.style = XlsxCellStyle.STYLE_ERROR;
        return this;
    }

    public CellArg readonly(boolean readonly) {
        if(readonly){
            return readonly();
        }
        return this;
    }

    public CellArg error(boolean error) {
        if(error){
            return error();
        }
        return this;
    }

    public CellArg title() {
        this.style = XlsxCellStyle.STYLE_TITLE;
        return this;
    }

    public CellArg key(String key) {
        this.key = key;
        return this;
    }

    public CellArg value(String value) {
        this.value = value;
        return this;
    }

    public CellArg annotations(String annotations) {
        this.annotations = annotations;
        return this;
    }

    public CellArg columnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
        return this;
    }

    public CellArg rowHeight(short rowHeight) {
        this.rowHeight = rowHeight;
        return this;
    }

    public CellArg combobox(String[] comboboxOptions) {
        this.type = XlsxExporter.TYPE_COMBOBOX;
        this.comboboxOptions = comboboxOptions;
        return this;
    }

    /**
     * 级联下拉
     * @param cascadeColIndex 根据哪一列进行级联，index从1开始
     * @param comboboxSubOptionMap 级联下拉可选子项
     * @return this
     */
    public CellArg comboboxIndirect(int cascadeColIndex, Map<String, String[]> comboboxSubOptionMap) {
        this.cascadeColIndex = cascadeColIndex;
        this.comboboxSubOptionMap = comboboxSubOptionMap;
        this.type = XlsxExporter.TYPE_COMBOBOX_INDIRECT;
        return this;
    }

    public CellArg redFont() {
        this.style = XlsxCellStyle.STYLE_RED_FONT;
        return this;
    }

    public CellArg leftTop() {
        this.style = XlsxCellStyle.STYLE_LEFT_TOP;
        return this;
    }

    public CellArg wrapText(){
        this.wrapText = true;
        return this;
    }

}
