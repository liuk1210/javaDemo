package com.demo.poi.xlsx;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class XlsxSaxReader {

    /**
     * 使用 SAX 模式读取 XLSX 文件的所有内容并返回一个二维列表。
     *
     * @param filePath XLSX 文件的路径
     * @return 包含所有数据的二维列表，每个内部列表代表一行数据
     * @throws Exception 如果文件读取失败
     */
    public static List<List<String>> read(String filePath) throws Exception {
        // 打开文件
        try (OPCPackage pkg = OPCPackage.open(new File(filePath))) {
            XSSFReader reader = new XSSFReader(pkg);

            // 获取共享字符串表
            SharedStringsTable sharedStringsTable = new SharedStringsTable();
            try (InputStream sharedStringsStream = reader.getSharedStringsData()) {
                sharedStringsTable.readFrom(sharedStringsStream);
            }

            // 获取样式表
            StylesTable stylesTable = reader.getStylesTable();

            // 获取合并区域信息
            List<CellRangeAddress> mergedRegions = new ArrayList<>();
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheetIterator.hasNext()) {
                InputStream sheetStream = sheetIterator.next();
                InputSource sheetSource = new InputSource(sheetStream);
                XMLReader parser = XMLReaderFactory.createXMLReader();
                MergedRegionHandler mergedRegionHandler = new MergedRegionHandler(mergedRegions);
                parser.setContentHandler(mergedRegionHandler);
                parser.parse(sheetSource);
            }

            // 创建 SAX 解析器
            XMLReader parser = XMLReaderFactory.createXMLReader();
            SheetHandler sheetHandler = new SheetHandler(mergedRegions);
            XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(
                    stylesTable, sharedStringsTable, sheetHandler, false
            );
            parser.setContentHandler(handler);

            // 获取第一个工作表的输入流
            InputStream sheetInputStream = reader.getSheetsData().next();
            InputSource sheetSource = new InputSource(sheetInputStream);

            // 解析工作表
            parser.parse(sheetSource);

            // 返回解析的数据
            return sheetHandler.getData();
        }
    }

    /**
     * 自定义 SheetHandler 类，用于处理 SAX 事件并提取数据。
     */
    @Getter
    private static class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final List<List<String>> data = new ArrayList<>();
        private List<String> currentRow = new ArrayList<>();
        private final List<CellRangeAddress> mergedRegions;
        private final Map<CellRangeAddress, String> mergedRegionValues = new HashMap<>();
        private int maxColumns = 0; // 记录最大列数

        public SheetHandler(List<CellRangeAddress> mergedRegions) {
            this.mergedRegions = mergedRegions;
        }

        @Override
        public void startRow(int rowNum) {
            // 开始新的一行
            currentRow = new ArrayList<>();
        }

        @Override
        public void endRow(int rowNum) {
            // 补全当前行的缺失列
            while (currentRow.size() < maxColumns) {
                currentRow.add(""); // 用 null 补全缺失的单元格
            }

            // 更新最大列数
            if (currentRow.size() > maxColumns) {
                maxColumns = currentRow.size();
            }

            // 结束当前行，将其添加到数据列表中
            data.add(new ArrayList<>(currentRow));
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            // 处理单元格数据
            int columnIndex = getColumnIndex(cellReference);
            int rowIndex = getRowIndex(cellReference);

            // 检查单元格是否属于合并区域的左上角第一个单元格
            for (CellRangeAddress mergedRegion : mergedRegions) {
                if (mergedRegion.getFirstRow() == rowIndex && mergedRegion.getFirstColumn() == columnIndex) {
                    // 缓存合并区域的值
                    mergedRegionValues.put(mergedRegion, formattedValue);
                }
            }

            // 检查单元格是否属于合并区域
            for (CellRangeAddress mergedRegion : mergedRegions) {
                if (mergedRegion.isInRange(rowIndex, columnIndex)) {
                    // 如果是合并区域的单元格，填充合并区域的值
                    String mergedValue = getMergedValue(mergedRegion);
                    // 确保当前行有足够的列
                    while (currentRow.size() <= columnIndex) {
                        currentRow.add(""); // 用 null 补全缺失的单元格
                    }
                    currentRow.set(columnIndex, mergedValue);
                    return;
                }
            }

            // 如果不是合并区域的单元格，直接添加值
            // 确保当前行有足够的列
            while (currentRow.size() <= columnIndex) {
                currentRow.add(""); // 用 null 补全缺失的单元格
            }
            currentRow.set(columnIndex, formattedValue);
        }

        /**
         * 获取单元格的列索引。
         *
         * @param cellReference 单元格引用（如 "A1"）
         * @return 列索引
         */
        private int getColumnIndex(String cellReference) {
            String columnPart = cellReference.replaceAll("[0-9]", "");
            int columnIndex = 0;
            for (int i = 0; i < columnPart.length(); i++) {
                columnIndex = columnIndex * 26 + (columnPart.charAt(i) - 'A' + 1);
            }
            return columnIndex - 1; // 转换为 0 基索引
        }

        /**
         * 获取单元格的行索引。
         *
         * @param cellReference 单元格引用（如 "A1"）
         * @return 行索引
         */
        private int getRowIndex(String cellReference) {
            String rowPart = cellReference.replaceAll("[^0-9]", "");
            return Integer.parseInt(rowPart) - 1; // 转换为 0 基索引
        }

        /**
         * 获取合并区域的值。
         *
         * @param mergedRegion 合并区域
         * @return 合并区域的值
         */
        private String getMergedValue(CellRangeAddress mergedRegion) {
            // 从缓存中获取合并区域的值
            return mergedRegionValues.getOrDefault(mergedRegion, "");
        }
    }

    /**
     * 自定义 MergedRegionHandler 类，用于解析合并区域信息。
     */
    private static class MergedRegionHandler extends org.xml.sax.helpers.DefaultHandler {
        private final List<CellRangeAddress> mergedRegions;

        public MergedRegionHandler(List<CellRangeAddress> mergedRegions) {
            this.mergedRegions = mergedRegions;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
            if (qName.equals("mergeCell")) {
                String ref = attributes.getValue("ref");
                mergedRegions.add(CellRangeAddress.valueOf(ref));
            }
        }
    }

    public static void main(String[] args) {
        try {
            String filePath = "demo.xlsx";
            List<List<String>> data = read(filePath);

            // 打印读取的数据
            for (List<String> row : data) {
                for (String cell : row) {
                    System.out.print(cell + "|");
                }
                System.out.println();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}