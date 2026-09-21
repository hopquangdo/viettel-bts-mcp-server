package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.impl;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.Map;

/**
 * Điền giá trị thật vào các placeholder dạng "@Token" trong file .docx mẫu (thay thế cơ chế
 * Find.Execute(Replace:=wdReplaceAll) của Word COM interop trong hệ thống VB.NET cũ). Token có
 * thể bị Word tách thành nhiều run trong cùng 1 đoạn văn (VD gõ có autocorrect) — nên phải gộp
 * text toàn bộ run của 1 đoạn lại trước khi tìm/thay, rồi ghi đè vào run đầu tiên để giữ định
 * dạng gốc, xoá nội dung các run còn lại.
 */
final class BienBanTemplateFiller {

    private BienBanTemplateFiller() {
    }

    static void fill(XWPFDocument doc, Map<String, String> values) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            fillParagraph(paragraph, values);
        }
        for (XWPFTable table : doc.getTables()) {
            fillTable(table, values);
        }
    }

    private static void fillTable(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    fillParagraph(paragraph, values);
                }
                for (XWPFTable nested : cell.getTables()) {
                    fillTable(nested, values);
                }
            }
        }
    }

    private static void fillParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        var runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) {
            return;
        }
        StringBuilder combined = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            combined.append(text == null ? "" : text);
        }
        String original = combined.toString();
        if (!original.contains("@")) {
            return;
        }
        String replaced = original;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            replaced = replaced.replace(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }
        if (replaced.equals(original)) {
            return;
        }
        runs.get(0).setText(replaced, 0);
        for (int i = 1; i < runs.size(); i++) {
            runs.get(i).setText("", 0);
        }
    }
}
