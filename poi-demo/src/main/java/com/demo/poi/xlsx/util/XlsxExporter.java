package com.demo.poi.xlsx.util;

import com.alibaba.fastjson2.JSONObject;
import com.demo.poi.xlsx.arg.CellArg;
import com.demo.poi.xlsx.arg.SheetArg;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * excel导出工具类
 */
@Slf4j
public class XlsxExporter {

    /**
     * 单元格数据展示类型
     */
    public static final String TYPE_STRING = "string", TYPE_COMBOBOX = "combobox", TYPE_COMBOBOX_INDIRECT = "comboboxIndirect";

    /**
     * 单sheet导出，用于web服务下载导出的文件
     *
     * @param fileName 文件名称
     * @param arg      参数
     * @param response 响应
     */
    public static void export(String fileName, SheetArg arg, HttpServletResponse response) {
        XlsxFileDownloader.download(fileName, response, exportExcel(arg));
    }

    /**
     * 单sheet导出，用于本地调试生成文件到本地
     * @param filePath 本地保存xlsx文件的路径
     * @param arg 参数
     */
    public static void export(String filePath, SheetArg arg) {
        XSSFWorkbook wb = exportExcel(arg);
        XlsxFileWriter.writeWorkbookToFile(wb,filePath);
    }

    /**
     * 大数据量导出
     *
     * @param fileName 文件名称
     * @param dataList 数据
     * @param response 响应
     */
    public static void exportBigData(String fileName, List<JSONObject> dataList, HttpServletResponse response) {
        XlsxFileDownloader.download(fileName, response, exportBigExcel(dataList));
    }

    private static SXSSFWorkbook exportBigExcel(List<JSONObject> dataList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("Sheet1");
        if (CollectionUtils.isEmpty(dataList)) {
            return workbook;
        }
        int rowIndex = 0;
        //初始化标题行
        JSONObject data = dataList.get(rowIndex);
        Row titleRow = sheet.createRow(rowIndex);
        List<String> title = new ArrayList<>();
        int index = 0;
        for (String key : data.keySet()) {
            titleRow.createCell(index).setCellValue(key);
            title.add(key);
            index++;
        }
        //初始化数据行
        for (JSONObject obj : dataList) {
            rowIndex++;
            Row row = sheet.createRow(rowIndex);
            for (int i = 0; i < title.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(obj.getString(title.get(i)));
            }
        }
        return workbook;
    }

    //单个sheet导出
    private static XSSFWorkbook exportExcel(SheetArg arg) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        initSheet(workbook, arg);
        return workbook;
    }

    //初始化sheet
    private static void initSheet(XSSFWorkbook workbook, SheetArg arg) {
        //校验sheetName是否合法
        validSheetName(arg.getName());

        XSSFSheet sheet = workbook.createSheet(arg.getName());
        List<List<CellArg>> template = arg.getTemplate();
        List<List<CellArg>> title = arg.getTitle();
        List<List<CellArg>> data = arg.getData();

        //表头开始行，即模板的行数
        int titleStartRow = template.size();
        //数据开始行，即模板+表头的总行数
        int dataStartRow = titleStartRow + title.size();
        //初始化样式
        Map<String, XSSFCellStyle> styleMap = XlsxCellStyle.initCellStyle(workbook);

        //1.初始化模板行
        for (int i = 0; i < template.size(); i++) {
            initRow(sheet.createRow(i), template.get(i), false, styleMap);
        }
        //2.初始化表内容
        for (int i = 0; i < data.size(); i++) {
            initRow(sheet.createRow(i + dataStartRow), data.get(i), true, styleMap);
        }
        //3.初始化表头，因为下拉框数据重复设置时第一次赋值的才有效
        for (int i = 0; i < title.size(); i++) {
            List<CellArg> cellArgs = title.get(i);
            XSSFRow row = sheet.createRow(i + titleStartRow);
            initRow(row, cellArgs, false, styleMap);
            //仅在最后一行表头初始化完毕后执行
            if (i == title.size() - 1) {
                //默认初始化200行的下拉框，从数据开始行开始初始化
                if (CollectionUtils.isNotEmpty(cellArgs)) {
                    for (int j = 0; j < cellArgs.size(); j++) {
                        addValidationDataOptions(sheet, cellArgs.get(j), dataStartRow, 200, j);
                    }
                }
            }
        }
        //3.1合并横向、纵向值相同的表头
        XlsxTitleMerger.mergeHorizontalAndVerticalCells(sheet, title);
        //4.合并单元格
        if (CollectionUtils.isNotEmpty(arg.getMergedCellRangeAddress())) {
            arg.getMergedCellRangeAddress().forEach(sheet::addMergedRegion);
        }
    }

    /**
     * 初始化一行数据
     *
     * @param row                       当前行
     * @param cells                     列参数
     * @param initValidationDataOptions 是否初始化枚举值下拉框
     * @param styleMap                  样式map
     */
    private static void initRow(XSSFRow row, List<CellArg> cells, boolean initValidationDataOptions, Map<String, XSSFCellStyle> styleMap) {
        if (CollectionUtils.isNotEmpty(cells)) {
            for (int j = 0; j < cells.size(); j++) {
                CellArg cellArg = cells.get(j);
                XSSFCell cell = row.createCell(j);

                //设置单元格专属枚举值
                if (initValidationDataOptions) {
                    addValidationDataOptions(row.getSheet(), cellArg, row.getRowNum(), row.getRowNum(), j);
                }

                //设置批注信息
                if (StringUtils.isNotBlank(cellArg.getAnnotations())) {
                    addAnnotationsComment(row, cell, cellArg.getAnnotations());
                }

                //设置单元格样式，需要换行处理的单独复制一份样式
                if (cellArg.isWrapText()) {
                    CellStyle cellStyle = styleMap.get(cellArg.getStyle()).copy();
                    cellStyle.setWrapText(true);
                    cell.setCellStyle(cellStyle);
                } else {
                    cell.setCellStyle(styleMap.get(cellArg.getStyle()));
                }

                //设置单元格宽度，仅在第一行行设置
                if (row.getRowNum() == 0) {
                    row.getSheet().setColumnWidth(j, cellArg.getColumnWidth());
                }

                //设置单元格高度
                if (cellArg.getRowHeight() != 0) {
                    row.setHeight(cellArg.getRowHeight());
                }

                //填充单元格数据，目前仅支持以下类型
                switch (cellArg.getType()) {
                    //字符串类型单元格
                    case TYPE_COMBOBOX_INDIRECT:
                    case TYPE_COMBOBOX:
                    case TYPE_STRING:
                        cell.setCellValue(cellArg.getValue());
                        cell.setCellType(CellType.STRING);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 设置批注信息
     *
     * @param row         行
     * @param cell        单元格
     * @param annotations 批注
     */
    private static void addAnnotationsComment(XSSFRow row, XSSFCell cell, String annotations) {
        ClientAnchor anchor = new XSSFClientAnchor();
        anchor.setDx1(0);
        anchor.setDx2(0);
        anchor.setDy1(0);
        anchor.setDy2(0);
        anchor.setCol1(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setCol2(cell.getColumnIndex() + 5);
        anchor.setRow2(cell.getRowIndex() + 6);
        Drawing<XSSFShape> drawing = row.getSheet().createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(annotations));
        cell.setCellComment(comment);
    }

    /**
     * 添加下拉框，分别有3种方式
     * 1为普通字符串选项下拉（小于255个字符时使用，防止创建过多sheet），
     * 2为生成sheet作为下拉选项（超过255个字符时使用），
     * 3为级联下拉
     *
     * @param sheet          当前sheet
     * @param cellArg        当前单元格参数
     * @param optionStartRow 起始行
     * @param optionEndRow   结束行
     * @param columnIndex    当前列index
     */
    private static void addValidationDataOptions(XSSFSheet sheet, CellArg cellArg, int optionStartRow, int optionEndRow, int columnIndex) {
        switch (cellArg.getType()) {
            case TYPE_COMBOBOX:
                if (cellArg.getComboboxOptions() == null || cellArg.getComboboxOptions().length == 0) {
                    break;
                }
                if (String.join(",", cellArg.getComboboxOptions()).length() < 255) {
                    //普通字符串选项下拉（小于255个字符时使用，防止创建过多sheet）
                    addValidationDataOptionsNoCreateHiddenSheet(sheet, cellArg.getComboboxOptions(), optionStartRow, optionEndRow, columnIndex);
                } else {
                    //生成sheet作为下拉选项（超过255个字符时）
                    addSheetValidationDataOptions(sheet, cellArg, optionStartRow, optionEndRow, columnIndex);
                }
                break;
            case TYPE_COMBOBOX_INDIRECT:
                if(cellArg.getComboboxSubOptionMap() == null || cellArg.getComboboxSubOptionMap().isEmpty()) {
                    break;
                }
                //3为级联下拉
                addIndirectValidationDataOptions(sheet, cellArg, optionStartRow, optionEndRow, columnIndex);
                break;
            default:
                break;
        }
    }

    /**
     * 在小于255个字符的情况下使用该方法创建下拉框，避免创建过多隐藏sheet
     *
     * @param sheet          需要添加下拉框的sheet
     * @param options        下拉框可选项
     * @param optionStartRow 下拉框选项开始行
     * @param optionEndRow   下拉框选项结束行
     * @param columnIndex    列index
     */
    private static void addValidationDataOptionsNoCreateHiddenSheet(XSSFSheet sheet, String[] options, int optionStartRow, int optionEndRow, int columnIndex) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(options);
        CellRangeAddressList addressList = new CellRangeAddressList(optionStartRow, optionEndRow, columnIndex, columnIndex);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    //生成sheet的方式做下拉框，可解决枚举值超过255个字符的问题，此列可作为被级联的列，被级联时需要使用此方式
    private static void addSheetValidationDataOptions(XSSFSheet sheet, CellArg cellArg, int optionStartRow, int optionEndRow, int columnIndex) {
        //创建一个sheet存储枚举值的下拉框数据
        XSSFSheet optionSheet = sheet.getWorkbook().createSheet();
        String optionSheetName = optionSheet.getSheetName();
        //隐藏下拉框sheet
        sheet.getWorkbook().setSheetHidden(sheet.getWorkbook().getSheetIndex(optionSheetName), true);

        //直接按照枚举值初始化
        String[] options = cellArg.getComboboxOptions();
        for (int i = 0; i < options.length; i++) {
            XSSFRow row = optionSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(options[i]);
        }
        String formula = optionSheetName + "!$A$1:$A$65535";
        XSSFDataValidationConstraint dvConstraint = new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.LIST, formula);
        CellRangeAddressList addressList = new CellRangeAddressList(optionStartRow, optionEndRow, columnIndex, columnIndex);
        DataValidationHelper help = new XSSFDataValidationHelper(sheet);
        DataValidation validation = help.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    //级联下拉框
    private static void addIndirectValidationDataOptions(XSSFSheet sheet, CellArg cellArg, int optionStartRow, int optionEndRow, int columnIndex) {
        //创建一个sheet存储枚举值的级联下拉框数据
        XSSFSheet optionSheet = sheet.getWorkbook().createSheet();
        String optionSheetName = optionSheet.getSheetName();
        //隐藏下拉框sheet
        sheet.getWorkbook().setSheetHidden(sheet.getWorkbook().getSheetIndex(optionSheetName), true);

        //初始化一个 excel公式-指定名称，作为列表源
        Map<String, String[]> indirectArgs = cellArg.getComboboxSubOptionMap();

        int index = 0;
        String nameNamePrefix = "n_";
        //用当前名称数量作为后缀
        int nameNameSuffix = sheet.getWorkbook().getNumberOfNames();
        for (String key : indirectArgs.keySet()) {
            XSSFRow row = optionSheet.createRow(index);
            Cell cell = row.createCell(0);
            cell.setCellValue(key);
            //创建子项
            String[] subName = indirectArgs.get(key);
            if (subName != null) {
                for (int j = 0; j < subName.length; j++) {
                    Cell subCell = row.createCell(j + 1);
                    subCell.setCellValue(subName[j]);
                }
                //每一个创建一个excel的名称
                Name name = sheet.getWorkbook().createName();
                String nameName = nameNamePrefix + key + nameNameSuffix;
                nameName = nameName.replaceAll("/", "");
                name.setNameName(nameName);
                int maxCol;
                if (subName.length == 0) {
                    //如果子选项为空，则默认给一个空格选项
                    maxCol = 2;
                } else {
                    maxCol = subName.length + 1;
                }
                String formula = optionSheetName + "!" + "$B$" + (index + 1)
                        + ":$" + convertToColumnName(maxCol) + "$" + (index + 1);
                name.setRefersToFormula(formula);
            }
            index++;
        }

        DataValidationHelper help = new XSSFDataValidationHelper(sheet);
        //每个单元格单独创建校验
        for (int i = optionStartRow; i <= optionEndRow; i++) {
            //将字符串/替换成空格，excel中name不支持/
            String substitute = "SUBSTITUTE(\"" + nameNamePrefix + "\"&$" + convertToColumnName(cellArg.getCascadeColIndex()) + "$" + (i + 1)
                    + "&\"" + nameNameSuffix + "\",\"/\",\"\")";
            DataValidation childValidation = help.createValidation(
                    help.createFormulaListConstraint(
                            "INDIRECT(" + substitute + ")"),
                    new CellRangeAddressList(i, i, columnIndex, columnIndex));
            childValidation.setShowErrorBox(true);
            childValidation.setSuppressDropDownArrow(true);
            sheet.addValidationData(childValidation);
        }

    }

    //列索引转换成excel的列
    public static String convertToColumnName(int num) {
        StringBuilder columnName = new StringBuilder();
        while (num > 0) {
            num--;
            columnName.insert(0, (char) (num % 26 + 'A'));
            num /= 26;
        }
        return columnName.toString();
    }

    //校验表格sheet名称
    private static final Pattern SHEET_NAME_PATTERN = Pattern.compile(":\\\\/\\?\\*\\[]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL | Pattern.MULTILINE);

    private static void validSheetName(String sheetName) {
        if (StringUtils.isEmpty(sheetName)) {
            throw new RuntimeException("导出文档中的sheet名称不能为空！");
        }
        if (sheetName.length() > 31) {
            throw new RuntimeException("导出文档中的sheet名称不能多于31个字符！");
        }
        if (SHEET_NAME_PATTERN.matcher(sheetName).find()) {
            throw new RuntimeException("导出文档中的sheet名称不能包含下列任一字符: :\\/?*[或]！");
        }
    }

}
