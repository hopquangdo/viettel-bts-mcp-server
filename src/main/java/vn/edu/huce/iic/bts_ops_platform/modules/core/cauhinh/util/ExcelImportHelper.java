package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.DoiTuongImportFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ExcelImportHelper {

    private static final DataFormatter FORMATTER = new DataFormatter();
    private static final int DEFAULT_DATA_START_ROW = 2;

    private ExcelImportHelper() {
    }

    public static List<String> listSheetNames(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return List.of();
        }
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                names.add(workbook.getSheetName(i));
            }
            return names;
        }
    }

    public static ExcelParseResult parseMappedWorkbook(
            MultipartFile file,
            String tenSheet,
            List<ExcelMappingCot> columns,
            Map<UUID, String> thuocTinhNames,
            UUID doiTuongQuanLyId,
            int dataStartRow,
            int maxRows,
            String cotNhomUuTien) throws IOException {
        if (file == null || file.isEmpty()) {
            return new ExcelParseResult(List.of(), List.of());
        }

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            Sheet sheet = resolveSheet(workbook, tenSheet);
            if (sheet == null || columns == null || columns.isEmpty()) {
                return new ExcelParseResult(sheetNames, List.of());
            }
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<Map<String, Object>> rows = parseSheetRows(
                    sheet, columns, thuocTinhNames, doiTuongQuanLyId, dataStartRow, maxRows, cotNhomUuTien, evaluator);
            return new ExcelParseResult(sheetNames, rows);
        }
    }

    public static List<Map<String, Object>> parseRows(
            MultipartFile file,
            String tenSheet,
            List<ExcelMappingCot> columns,
            Map<UUID, String> thuocTinhNames,
            UUID doiTuongQuanLyId,
            int dataStartRow,
            int maxRows,
            String cotNhomUuTien) throws IOException {
        if (file == null || file.isEmpty() || columns == null || columns.isEmpty()) {
            return List.of();
        }

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = resolveSheet(workbook, tenSheet);
            if (sheet == null) {
                return List.of();
            }
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            return parseSheetRows(
                    sheet, columns, thuocTinhNames, doiTuongQuanLyId, dataStartRow, maxRows, cotNhomUuTien, evaluator);
        }
    }

    private static List<Map<String, Object>> parseSheetRows(
            Sheet sheet,
            List<ExcelMappingCot> columns,
            Map<UUID, String> thuocTinhNames,
            UUID doiTuongQuanLyId,
            int dataStartRow,
            int maxRows,
            String cotNhomUuTien,
            FormulaEvaluator evaluator) {
            List<ExcelResolvedColumn> resolved = resolveColumns(columns);
            if (resolved.isEmpty()) {
                return List.of();
            }

            Integer nhomUuTienColumnIndex = resolveNhomUuTienColumnIndex(doiTuongQuanLyId, cotNhomUuTien);

            boolean allowGroupParsing = HangMucImportFields.DOI_TUONG_ID
                    .equals(doiTuongQuanLyId);
            List<ExcelResolvedColumn> hierarchyColumns = allowGroupParsing
                    ? resolved.stream()
                            .filter(item -> isHierarchyInheritanceColumn(item, thuocTinhNames))
                            .sorted(Comparator.comparingInt(
                                    item -> resolveHierarchyCapFromFieldName(item.fieldName(thuocTinhNames))))
                            .toList()
                    : List.of();

            List<Map<String, Object>> rows = new ArrayList<>();
            Map<Short, String> currentHierarchy = new HashMap<>();
            int lastRow = resolveLastRowNum(sheet);
            int startRow = dataStartRow > 0 ? dataStartRow : DEFAULT_DATA_START_ROW;
            int dataRowStartIndex = Math.max(startRow - 1, 0);

            if (!hierarchyColumns.isEmpty() && dataRowStartIndex > 0) {
                for (int rowIndex = 0; rowIndex < dataRowStartIndex; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        updateHierarchyState(row, hierarchyColumns, thuocTinhNames, currentHierarchy, evaluator);
                    }
                }
            }

            for (int rowIndex = dataRowStartIndex; rowIndex <= lastRow; rowIndex++) {
                if (rows.size() >= maxRows) {
                    break;
                }
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                if (!hierarchyColumns.isEmpty()) {
                    updateHierarchyState(row, hierarchyColumns, thuocTinhNames, currentHierarchy, evaluator);

                    if (isRowEmptyForHierarchy(row, resolved, thuocTinhNames, evaluator)) {
                        continue;
                    }

                    Map<String, Object> item = buildHierarchyRowItem(
                            row,
                            resolved,
                            thuocTinhNames,
                            hierarchyColumns,
                            currentHierarchy,
                            nhomUuTienColumnIndex,
                            evaluator);
                    if (!item.isEmpty()) {
                        rows.add(item);
                    }
                    continue;
                }

                if (isRowEmpty(row, resolved, nhomUuTienColumnIndex, evaluator)) {
                    continue;
                }
                Map<String, Object> item = buildRowItem(row, resolved, thuocTinhNames, nhomUuTienColumnIndex, evaluator);
                if (!item.isEmpty()) {
                    rows.add(item);
                }
            }
            return rows;
    }

    private static boolean isHierarchyInheritanceColumn(
            ExcelResolvedColumn resolved,
            Map<UUID, String> thuocTinhNames) {
        Short cap = resolveHierarchyCapFromFieldName(resolved.fieldName(thuocTinhNames));
        return cap != null && cap <= 2;
    }

    private static Short resolveHierarchyCapFromFieldName(String fieldName) {
        return HangMucImportFields.hierarchyCap(fieldName);
    }

    private static void updateHierarchyState(
            Row row,
            List<ExcelResolvedColumn> hierarchyColumns,
            Map<UUID, String> thuocTinhNames,
            Map<Short, String> currentHierarchy,
            FormulaEvaluator evaluator) {
        for (ExcelResolvedColumn hierarchyColumn : hierarchyColumns) {
            Short cap = resolveHierarchyCapFromFieldName(hierarchyColumn.fieldName(thuocTinhNames));
            if (cap == null) {
                continue;
            }
            String value = readCell(row.getCell(hierarchyColumn.columnIndex()), evaluator);
            if (!value.isBlank()) {
                currentHierarchy.put(cap, value);
                if (cap == 1) {
                    currentHierarchy.remove((short) 2);
                }
            }
        }
    }

    private static boolean isRowEmptyForHierarchy(
            Row row,
            List<ExcelResolvedColumn> resolved,
            Map<UUID, String> thuocTinhNames,
            FormulaEvaluator evaluator) {
        for (ExcelResolvedColumn resolvedColumn : resolved) {
            if (isHierarchyInheritanceColumn(resolvedColumn, thuocTinhNames)) {
                continue;
            }
            if (!readCell(row.getCell(resolvedColumn.columnIndex()), evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> buildHierarchyRowItem(
            Row row,
            List<ExcelResolvedColumn> resolved,
            Map<UUID, String> thuocTinhNames,
            List<ExcelResolvedColumn> hierarchyColumns,
            Map<Short, String> currentHierarchy,
            Integer nhomUuTienColumnIndex,
            FormulaEvaluator evaluator) {
        Map<String, Object> item = new LinkedHashMap<>();
        for (ExcelResolvedColumn resolvedColumn : resolved) {
            Short cap = resolveHierarchyCapFromFieldName(resolvedColumn.fieldName(thuocTinhNames));
            String value;
            if (isHierarchyInheritanceColumn(resolvedColumn, thuocTinhNames) && cap != null) {
                String cellValue = readCell(row.getCell(resolvedColumn.columnIndex()), evaluator);
                value = cellValue.isBlank()
                        ? currentHierarchy.getOrDefault(cap, "")
                        : cellValue;
            } else {
                value = readCell(row.getCell(resolvedColumn.columnIndex()), evaluator);
            }
            String fieldName = resolvedColumn.fieldName(thuocTinhNames);
            item.put(fieldName, value.isBlank() ? null : value);
        }
        appendNhomUuTien(item, row, nhomUuTienColumnIndex, evaluator);
        return item;
    }

    private static int resolveLastRowNum(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        int maxPhysical = -1;
        for (Row row : sheet) {
            if (row != null) {
                maxPhysical = Math.max(maxPhysical, row.getRowNum());
            }
        }
        if (maxPhysical < 0) {
            return lastRow;
        }
        return Math.max(lastRow, maxPhysical);
    }

    private static Map<String, Object> buildRowItem(
            Row row,
            List<ExcelResolvedColumn> resolved,
            Map<UUID, String> thuocTinhNames,
            Integer nhomUuTienColumnIndex,
            FormulaEvaluator evaluator) {
        Map<String, Object> item = new LinkedHashMap<>();
        for (ExcelResolvedColumn resolvedColumn : resolved) {
            String value = readCell(row.getCell(resolvedColumn.columnIndex()), evaluator);
            String fieldName = resolvedColumn.fieldName(thuocTinhNames);
            item.put(fieldName, value.isBlank() ? null : value);
        }
        appendNhomUuTien(item, row, nhomUuTienColumnIndex, evaluator);
        return item;
    }

    private static void appendNhomUuTien(
            Map<String, Object> item,
            Row row,
            Integer nhomUuTienColumnIndex,
            FormulaEvaluator evaluator) {
        if (nhomUuTienColumnIndex == null) {
            return;
        }
        String value = readCell(row.getCell(nhomUuTienColumnIndex), evaluator);
        item.put(DoiTuongImportFields.NHOM_UU_TIEN, value.isBlank() ? null : value);
    }

    private static Integer resolveNhomUuTienColumnIndex(UUID doiTuongQuanLyId, String cotNhomUuTien) {
        if (!DoiTuongImportFields.supportsNhomUuTien(doiTuongQuanLyId)) {
            return null;
        }
        if (cotNhomUuTien == null || cotNhomUuTien.isBlank()) {
            return null;
        }
        return columnLetterToIndex(cotNhomUuTien);
    }

    private static Sheet resolveSheet(Workbook workbook, String tenSheet) {
        if (tenSheet != null && !tenSheet.isBlank()) {
            Sheet sheet = workbook.getSheet(tenSheet);
            if (sheet != null) {
                return sheet;
            }
        }
        return workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
    }

    private static List<ExcelResolvedColumn> resolveColumns(List<ExcelMappingCot> columns) {
        List<ExcelResolvedColumn> resolved = new ArrayList<>();
        for (ExcelMappingCot column : columns) {
            if (column.getCotExcel() == null || column.getCotExcel().isBlank()) {
                continue;
            }
            Integer columnIndex = columnLetterToIndex(column.getCotExcel());
            if (columnIndex != null) {
                resolved.add(new ExcelResolvedColumn(column, columnIndex));
            }
        }
        return resolved;
    }

    private static boolean isRowEmpty(
            Row row,
            List<ExcelResolvedColumn> resolved,
            Integer nhomUuTienColumnIndex,
            FormulaEvaluator evaluator) {
        for (ExcelResolvedColumn resolvedColumn : resolved) {
            String value = readCell(row.getCell(resolvedColumn.columnIndex()), evaluator);
            if (!value.isBlank()) {
                return false;
            }
        }
        if (nhomUuTienColumnIndex != null) {
            String nhomValue = readCell(row.getCell(nhomUuTienColumnIndex), evaluator);
            if (!nhomValue.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String readCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        return FORMATTER.formatCellValue(cell, evaluator).trim();
    }

    private static Integer columnLetterToIndex(String letter) {
        String normalized = letter.trim().toUpperCase();
        if (normalized.isEmpty() || !normalized.matches("[A-Z]+")) {
            return null;
        }
        int result = 0;
        for (int i = 0; i < normalized.length(); i++) {
            result = result * 26 + (normalized.charAt(i) - 'A' + 1);
        }
        return result - 1;
    }

}
