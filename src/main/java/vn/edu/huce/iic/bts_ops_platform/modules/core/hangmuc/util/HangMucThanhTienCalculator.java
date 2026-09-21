package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucItemComputed;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucNhomComputed;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucRowComputed;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucRowType;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucThanhTienResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucKhoiLuongSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomTreeResponse;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Tính thành tiền hạng mục theo cùng quy tắc tab Hạng mục thi công:
 * KL ưu tiên trên hạng mục (số / công thức), fallback map sản lượng nếu chưa nhập;
 * DG/TT theo số tay hoặc công thức (@MÃ.KL, @SUM.TT, @KL*@DG...).
 */
public final class HangMucThanhTienCalculator {

    static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private HangMucThanhTienCalculator() {}

    public static HangMucThanhTienResult compute(HangMucHopDongTreeResponse tree) {
        HangMucThanhTienResult result = HangMucThanhTienResult.builder().build();
        if (tree == null || tree.getNhom() == null || tree.getNhom().isEmpty()) {
            return result;
        }

        HangMucKhoiLuongSanLuongResponse klMaps = tree.getKhoiLuongSanLuong() != null
                ? tree.getKhoiLuongSanLuong()
                : new HangMucKhoiLuongSanLuongResponse();

        HangMucCalcGrid grid = buildGrid(tree.getNhom(), klMaps);
        HangMucCalcEvalContext ctx = new HangMucCalcEvalContext(grid);

        for (HangMucCalcGridRow row : grid.rows) {
            HangMucRowComputed computed = HangMucRowComputed.builder()
                    .id(row.id)
                    .parentItemId(row.parentItemId)
                    .nhomId(row.nhomId)
                    .type(row.type)
                    .ma(row.ma)
                    .ten(row.ten)
                    .kl(safeField(row, HangMucCalcField.KL, ctx))
                    .dg(safeField(row, HangMucCalcField.DG, ctx))
                    .tt(safeField(row, HangMucCalcField.TT, ctx))
                    .build();
            result.getById().put(row.id, computed);
        }

        BigDecimal tong = BigDecimal.ZERO;
        for (HangMucNhomTreeResponse nhom : tree.getNhom()) {
            HangMucNhomComputed nhomComputed = HangMucNhomComputed.builder()
                    .id(nhom.getId())
                    .ma(nhom.getMa())
                    .ten(nhom.getTen())
                    .build();
            BigDecimal nhomTt = BigDecimal.ZERO;
            if (nhom.getChiTiet() != null) {
                for (HangMucChiTietTreeResponse item : nhom.getChiTiet()) {
                    HangMucRowComputed itemComputed = result.getById().get(item.getId());
                    if (itemComputed == null) continue;
                    HangMucItemComputed hangMuc = HangMucItemComputed.builder()
                            .id(item.getId())
                            .ma(item.getMa())
                            .ten(item.getTen())
                            .khoiLuong(itemComputed.getKl())
                            .donGia(itemComputed.getDg())
                            .thanhTien(itemComputed.getTt())
                            .build();
                    nhomComputed.getHangMuc().add(hangMuc);
                    nhomTt = nhomTt.add(itemComputed.getTt() != null ? itemComputed.getTt() : BigDecimal.ZERO);
                }
            }
            nhomComputed.setThanhTien(nhomTt);
            result.getNhom().add(nhomComputed);
            tong = tong.add(nhomTt);
        }
        result.setTongThanhTien(tong);
        return result;
    }

    private static BigDecimal safeField(HangMucCalcGridRow row, HangMucCalcField field, HangMucCalcEvalContext ctx) {
        try {
            return getFieldValue(row.rowNumber, field, ctx);
        } catch (RuntimeException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static HangMucCalcGrid buildGrid(List<HangMucNhomTreeResponse> nhomList, HangMucKhoiLuongSanLuongResponse klMaps) {
        HangMucCalcGrid grid = new HangMucCalcGrid();
        int rowNumber = 0;
        Map<UUID, BigDecimal> theoCongViec = klMaps.getTheoCongViec() != null ? klMaps.getTheoCongViec() : Map.of();
        Map<UUID, BigDecimal> theoChiTiet = klMaps.getTheoChiTiet() != null ? klMaps.getTheoChiTiet() : Map.of();

        for (HangMucNhomTreeResponse nhom : nhomList) {
            if (nhom.getChiTiet() == null) continue;
            for (HangMucChiTietTreeResponse item : nhom.getChiTiet()) {
                rowNumber += 1;
                boolean hasTasks = item.getCongViec() != null && !item.getCongViec().isEmpty();
                HangMucCalcGridRow itemRow = new HangMucCalcGridRow();
                itemRow.rowNumber = rowNumber;
                itemRow.id = item.getId();
                itemRow.nhomId = nhom.getId();
                itemRow.type = HangMucRowType.ITEM;
                itemRow.ma = item.getMa();
                itemRow.ten = item.getTen();
                itemRow.donGia = item.getDonGia();
                itemRow.congThucKhoiLuong = item.getCongThucKhoiLuong();
                itemRow.congThucDonGia = item.getCongThucDonGia();
                itemRow.congThucThanhTien = item.getCongThucThanhTien();
                if (!hasTasks) {
                    itemRow.khoiLuong = resolveKhoiLuong(
                            item.getKhoiLuong(),
                            item.getCongThucKhoiLuong(),
                            theoChiTiet.get(item.getId()));
                }
                grid.add(itemRow);

                if (hasTasks) {
                    for (HangMucCongViecResponse task : item.getCongViec()) {
                        rowNumber += 1;
                        HangMucCalcGridRow taskRow = new HangMucCalcGridRow();
                        taskRow.rowNumber = rowNumber;
                        taskRow.id = task.getId();
                        taskRow.nhomId = nhom.getId();
                        taskRow.type = HangMucRowType.TASK;
                        taskRow.ma = task.getMa();
                        taskRow.ten = task.getTen();
                        taskRow.donGia = task.getDonGia();
                        taskRow.congThucKhoiLuong = task.getCongThucKhoiLuong();
                        taskRow.congThucDonGia = task.getCongThucDonGia();
                        taskRow.congThucThanhTien = task.getCongThucThanhTien();
                        taskRow.khoiLuong = resolveKhoiLuong(
                                task.getKhoiLuong(),
                                task.getCongThucKhoiLuong(),
                                theoCongViec.get(task.getId()));
                        taskRow.parentItemId = item.getId();
                        grid.add(taskRow);
                    }
                }
            }
        }
        return grid;
    }

    static BigDecimal getFieldValue(int rowNumber, HangMucCalcField field, HangMucCalcEvalContext ctx) {
        String key = rowNumber + ":" + field;
        if (ctx.cache.containsKey(key)) {
            return ctx.cache.get(key);
        }
        if (ctx.stack.contains(key)) {
            throw new IllegalStateException("Tham chiếu vòng tại dòng " + rowNumber + "." + field);
        }
        ctx.stack.add(key);
        try {
            HangMucCalcGridRow row = ctx.grid.byRowNumber.get(rowNumber);
            if (row == null) throw new IllegalStateException("Không có dòng " + rowNumber);
            BigDecimal value = switch (field) {
                case KL -> computeKhoiLuong(row, ctx);
                case DG -> computeDonGia(row, ctx);
                case TT -> computeThanhTien(row, ctx);
            };
            ctx.cache.put(key, value);
            return value;
        } finally {
            ctx.stack.remove(key);
        }
    }

    private static BigDecimal computeKhoiLuong(HangMucCalcGridRow row, HangMucCalcEvalContext ctx) {
        if (shouldAutoSum(row, ctx.grid, HangMucCalcField.KL)) {
            return sumChildren(row, HangMucCalcField.KL, ctx);
        }
        if (hasText(row.congThucKhoiLuong)) {
            return parseExpression(row.congThucKhoiLuong, ctx, row.rowNumber, HangMucCalcField.KL);
        }
        return nz(row.khoiLuong);
    }

    /**
     * Ưu tiên KL entity / công thức trên hạng mục; fallback map sản lượng nếu chưa nhập.
     * Khi có công thức, để khoiLuong = null — computeKhoiLuong sẽ eval công thức.
     */
    private static BigDecimal resolveKhoiLuong(
            BigDecimal entityKl, String congThucKhoiLuong, BigDecimal sanLuongKl) {
        if (hasText(congThucKhoiLuong)) {
            return null;
        }
        // KL = 0 cũng coi là "chưa nhập" khi có sản lượng thực tế: dữ liệu import cũ bị ép 0
        // hàng loạt, và hạng mục đã thi công (có sản lượng) mà thành tiền 0 không có nghĩa.
        if (entityKl != null && entityKl.signum() != 0) {
            return entityKl;
        }
        if (sanLuongKl != null) {
            return sanLuongKl;
        }
        return entityKl != null ? entityKl : BigDecimal.ZERO;
    }

    private static BigDecimal computeDonGia(HangMucCalcGridRow row, HangMucCalcEvalContext ctx) {
        if (hasText(row.congThucDonGia)) {
            return parseExpression(row.congThucDonGia, ctx, row.rowNumber, HangMucCalcField.DG);
        }
        if (shouldAutoSum(row, ctx.grid, HangMucCalcField.DG)) {
            return sumChildren(row, HangMucCalcField.DG, ctx);
        }
        return nz(row.donGia);
    }

    private static BigDecimal computeThanhTien(HangMucCalcGridRow row, HangMucCalcEvalContext ctx) {
        if (hasText(row.congThucThanhTien)) {
            return parseExpression(row.congThucThanhTien, ctx, row.rowNumber, HangMucCalcField.TT);
        }
        if (shouldAutoSum(row, ctx.grid, HangMucCalcField.TT)) {
            return sumChildren(row, HangMucCalcField.TT, ctx);
        }
        return getFieldValue(row.rowNumber, HangMucCalcField.KL, ctx)
                .multiply(getFieldValue(row.rowNumber, HangMucCalcField.DG, ctx), MC);
    }

    private static boolean shouldAutoSum(HangMucCalcGridRow row, HangMucCalcGrid grid, HangMucCalcField field) {
        if (row.type != HangMucRowType.ITEM) return false;
        List<HangMucCalcGridRow> children = childrenOf(row, grid);
        if (children.isEmpty()) return false;
        if (field == HangMucCalcField.KL) return true;
        if (field == HangMucCalcField.DG) {
            if (hasText(row.congThucDonGia)) return false;
            return row.donGia == null || row.donGia.compareTo(BigDecimal.ZERO) == 0;
        }
        return !hasText(row.congThucThanhTien);
    }

    private static List<HangMucCalcGridRow> childrenOf(HangMucCalcGridRow row, HangMucCalcGrid grid) {
        return grid.childrenByItemId.getOrDefault(row.id, List.of());
    }

    static BigDecimal sumChildren(HangMucCalcGridRow row, HangMucCalcField field, HangMucCalcEvalContext ctx) {
        BigDecimal sum = BigDecimal.ZERO;
        for (HangMucCalcGridRow child : childrenOf(row, ctx.grid)) {
            sum = sum.add(getFieldValue(child.rowNumber, field, ctx));
        }
        return sum;
    }

    private static BigDecimal parseExpression(String formula, HangMucCalcEvalContext ctx, int currentRow, HangMucCalcField defaultField) {
        String normalized = formula == null ? "" : formula.trim().replaceFirst("^=", "").trim();
        if (normalized.isEmpty()) return BigDecimal.ZERO;
        List<HangMucCalcToken> tokens = tokenize(normalized);
        HangMucCalcParser parser = new HangMucCalcParser(tokens, ctx, currentRow, defaultField);
        BigDecimal result = parser.parseAddSub();
        if (parser.index < tokens.size()) {
            throw new IllegalStateException("Biểu thức có phần thừa: " + formula);
        }
        return result;
    }

    private static List<HangMucCalcToken> tokenize(String input) {
        List<HangMucCalcToken> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (Character.isWhitespace(ch)) {
                i += 1;
                continue;
            }
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                tokens.add(HangMucCalcToken.op(ch));
                i += 1;
                continue;
            }
            if (ch == '(') {
                tokens.add(HangMucCalcToken.lparen());
                i += 1;
                continue;
            }
            if (ch == ')') {
                tokens.add(HangMucCalcToken.rparen());
                i += 1;
                continue;
            }
            if (ch == '%') {
                tokens.add(HangMucCalcToken.percent());
                i += 1;
                continue;
            }
            if (Character.isDigit(ch) || ch == '.') {
                int start = i;
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i += 1;
                }
                tokens.add(HangMucCalcToken.number(new BigDecimal(input.substring(start, i))));
                continue;
            }
            if (ch == '@') {
                int start = i + 1;
                i = start;
                while (i < input.length()) {
                    char c = input.charAt(i);
                    if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == 'Ã'
                            || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.LATIN_1_SUPPLEMENT
                            || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.LATIN_EXTENDED_A
                            || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.LATIN_EXTENDED_B
                            || isVietnameseLetter(c)) {
                        i += 1;
                        continue;
                    }
                    break;
                }
                String raw = input.substring(start, i).trim();
                tokens.add(parseAtRef(raw));
                continue;
            }
            throw new IllegalStateException("Ký tự không hợp lệ trong công thức: " + ch);
        }
        return tokens;
    }

    private static boolean isVietnameseLetter(char c) {
        return "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ"
                .indexOf(Character.toUpperCase(c)) >= 0
                || "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ"
                .indexOf(c) >= 0;
    }

    private static HangMucCalcToken parseAtRef(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        if (upper.equals("KL") || upper.equals("DG") || upper.equals("TT")
                || upper.equals("KHOILUONG") || upper.equals("DONGIA") || upper.equals("THANHTIEN")) {
            return HangMucCalcToken.selfRef(parseField(upper));
        }
        if (upper.startsWith("SUM.")) {
            return HangMucCalcToken.sumRef(parseField(upper.substring(4)));
        }
        int dot = raw.lastIndexOf('.');
        if (dot > 0) {
            String ma = raw.substring(0, dot);
            HangMucCalcField field = parseField(raw.substring(dot + 1));
            return HangMucCalcToken.maRef(normalizeMa(ma), field);
        }
        return HangMucCalcToken.maRef(normalizeMa(raw), null);
    }

    private static HangMucCalcField parseField(String raw) {
        String upper = raw.trim().toUpperCase(Locale.ROOT)
                .replace("Đ", "D")
                .replace("Á", "A")
                .replace("À", "A");
        return switch (upper) {
            case "KL", "KHOILUONG", "KHỐI LƯỢNG", "KHOI LUONG" -> HangMucCalcField.KL;
            case "DG", "DONGIA", "ĐG", "ĐƠN GIÁ", "DON GIA" -> HangMucCalcField.DG;
            case "TT", "THANHTIEN", "THÀNH TIỀN", "THANH TIEN" -> HangMucCalcField.TT;
            default -> throw new IllegalStateException("Trường không hợp lệ: " + raw);
        };
    }

    static String normalizeMa(String ma) {
        return ma == null ? "" : ma.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
