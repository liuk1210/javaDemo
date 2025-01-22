package com.demo.poi.xlsx.util;

import com.demo.poi.xlsx.arg.CellArg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

public class XlsxTitleMerger {

    /**
     * 合并横向和纵向的重复单元格
     *
     * @param sheet    工作表
     * @param headers  表头数据
     */
    public static void mergeHorizontalAndVerticalCells(Sheet sheet, List<List<CellArg>> headers) {
        int rowCount = headers.size();
        int colCount = headers.get(0).size();

        // 横向合并
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            mergeDuplicateCellsInRow(sheet, rowIndex);
        }

        // 纵向合并
        for (int colIndex = 0; colIndex < colCount; colIndex++) {
            mergeDuplicateCellsInColumn(sheet, colIndex, rowCount);
        }
    }

    /**
     * 合并指定行中重复的单元格（横向合并）
     *
     * @param sheet    工作表
     * @param rowIndex 需要合并的行索引
     */
    private static void mergeDuplicateCellsInRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return;

        int startCol = 0;
        String prevValue = "";

        for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
            Cell cell = row.getCell(colIndex);
            String currentValue = cell != null ? cell.getStringCellValue() : "";

            if (currentValue.equals(prevValue)) {
                // 如果当前单元格的值与前一个单元格的值相同，继续
                continue;
            } else {
                // 如果值不同，检查是否需要合并
                if (colIndex - startCol > 1) {
                    // 合并单元格
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startCol, colIndex - 1));
                }
                // 更新起始列和上一个值
                startCol = colIndex;
                prevValue = currentValue;
            }
        }

        // 处理最后一组重复值
        if (row.getLastCellNum() - startCol > 1) {
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startCol, row.getLastCellNum() - 1));
        }
    }

    /**
     * 合并指定列中重复的单元格（纵向合并）
     *
     * @param sheet    工作表
     * @param colIndex 需要合并的列索引
     * @param rowCount 总行数
     */
    private static void mergeDuplicateCellsInColumn(Sheet sheet, int colIndex, int rowCount) {
        int startRow = 0;
        String prevValue = "";

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            Cell cell = row != null ? row.getCell(colIndex) : null;
            String currentValue = cell != null ? cell.getStringCellValue() : "";

            if (currentValue.equals(prevValue)) {
                // 如果当前单元格的值与前一个单元格的值相同，继续
                continue;
            } else {
                // 如果值不同，检查是否需要合并
                if (rowIndex - startRow > 1) {
                    // 合并单元格
                    sheet.addMergedRegion(new CellRangeAddress(startRow, rowIndex - 1, colIndex, colIndex));
                }
                // 更新起始行和上一个值
                startRow = rowIndex;
                prevValue = currentValue;
            }
        }

        // 处理最后一组重复值
        if (rowCount - startRow > 1) {
            sheet.addMergedRegion(new CellRangeAddress(startRow, rowCount - 1, colIndex, colIndex));
        }
    }

}
