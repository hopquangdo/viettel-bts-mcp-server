package vn.edu.huce.iic.bts_ops_platform.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Port thuần Java từ {@code modules.core.hangmuc.util.HangMucThanhTienCalculator} (+ các lớp vệ
 * tinh HangMucCalcGrid/EvalContext/Parser/Token) — engine tính KL/DG/TT cho cây hạng mục thi công,
 * hỗ trợ công thức kiểu Excel (@MA.KL, @SUM.TT, tự tổng hợp từ dòng con). Tools không được import
 * service/repository của module hangmuc nên nhân bản đúng thuật toán tại đây, chỉ đọc dữ liệu qua
 * NganHoToolRepository (native SQL trên hang_muc_nhom/hang_muc_chi_tiet/hang_muc_cong_viec).
 */
public final class NganHoFormulaCalc {

    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private NganHoFormulaCalc() {
    }

    public record NhomRow(UUID id, String ma, String ten) {
    }

    public record ChiTietRow(UUID id, UUID nhomId, String ma, String ten, BigDecimal donGia, BigDecimal khoiLuong,
                              String congThucKhoiLuong, String congThucDonGia, String congThucThanhTien) {
    }

    public record CongViecRow(UUID id, UUID chiTietId, String ma, String ten, BigDecimal donGia, BigDecimal khoiLuong,
                               String congThucKhoiLuong, String congThucDonGia, String congThucThanhTien) {
    }

    public record HangMucItem(UUID id, String ma, String ten, BigDecimal khoiLuong, BigDecimal donGia, BigDecimal thanhTien) {
    }

    public record NhomResult(UUID id, String ma, String ten, BigDecimal thanhTien, List<HangMucItem> hangMuc) {
    }

    public record ComputeResult(BigDecimal tongThanhTien, List<NhomResult> nhom) {
    }

    public static ComputeResult compute(List<NhomRow> nhomRows, List<ChiTietRow> chiTietRows, List<CongViecRow> congViecRows,
                                        Map<UUID, BigDecimal> theoCongViec, Map<UUID, BigDecimal> theoChiTiet) {
        if (nhomRows.isEmpty()) {
            return new ComputeResult(BigDecimal.ZERO, List.of());
        }
        Map<UUID, List<CongViecRow>> congViecByChiTiet = new HashMap<>();
        for (CongViecRow task : congViecRows) {
            congViecByChiTiet.computeIfAbsent(task.chiTietId(), k -> new ArrayList<>()).add(task);
        }
        Map<UUID, List<ChiTietRow>> chiTietByNhom = new HashMap<>();
        for (ChiTietRow item : chiTietRows) {
            chiTietByNhom.computeIfAbsent(item.nhomId(), k -> new ArrayList<>()).add(item);
        }

        Grid grid = buildGrid(nhomRows, chiTietByNhom, congViecByChiTiet, theoCongViec, theoChiTiet);
        EvalContext ctx = new EvalContext(grid);
        Map<UUID, BigDecimal[]> byId = new HashMap<>();
        for (GridRow row : grid.rows) {
            BigDecimal kl = safeField(row, Field.KL, ctx);
            BigDecimal dg = safeField(row, Field.DG, ctx);
            BigDecimal tt = safeField(row, Field.TT, ctx);
            byId.put(row.id, new BigDecimal[]{kl, dg, tt});
        }

        BigDecimal tong = BigDecimal.ZERO;
        List<NhomResult> nhomResults = new ArrayList<>();
        for (NhomRow nhom : nhomRows) {
            List<HangMucItem> items = new ArrayList<>();
            BigDecimal nhomTt = BigDecimal.ZERO;
            for (ChiTietRow item : chiTietByNhom.getOrDefault(nhom.id(), List.of())) {
                BigDecimal[] computed = byId.get(item.id());
                if (computed == null) {
                    continue;
                }
                items.add(new HangMucItem(item.id(), item.ma(), item.ten(), computed[0], computed[1], computed[2]));
                nhomTt = nhomTt.add(computed[2] != null ? computed[2] : BigDecimal.ZERO);
            }
            nhomResults.add(new NhomResult(nhom.id(), nhom.ma(), nhom.ten(), nhomTt, items));
            tong = tong.add(nhomTt);
        }
        return new ComputeResult(tong, nhomResults);
    }

    /** Ngưỡng % mặc định để cảnh báo (canh_bao / warning) - khớp VolumeTinhToanHelper.WARNING_RATIO bên REST. */
    public static final BigDecimal NGUONG_CANH_BAO_MAC_DINH = BigDecimal.valueOf(90);

    public static String resolveStatus(long giaTriHd, BigDecimal tongThiCong) {
        return resolveStatus(giaTriHd, tongThiCong, NGUONG_CANH_BAO_MAC_DINH);
    }

    /** Trạng thái so với ngưỡng — port VolumeTinhToanHelper.resolveStatus(giaTriHd, tongThiCong), ngưỡng cảnh báo truyền được. */
    public static String resolveStatus(long giaTriHd, BigDecimal tongThiCong, BigDecimal nguongCanhBao) {
        BigDecimal plan = BigDecimal.valueOf(giaTriHd);
        BigDecimal actual = nz(tongThiCong);
        if (plan.compareTo(BigDecimal.ZERO) <= 0) {
            return "binh_thuong";
        }
        if (actual.compareTo(plan) > 0) {
            return "vuot_nguong";
        }
        if (ratio(actual, plan).compareTo(nguongCanhBao) >= 0) {
            return "canh_bao";
        }
        return "binh_thuong";
    }

    public static BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return nz(numerator).multiply(HUNDRED, MC).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static String resolveProvinceVariant(long soTramThieu, long soTramThua, BigDecimal remaining, BigDecimal usedPercent) {
        return resolveProvinceVariant(soTramThieu, soTramThua, remaining, usedPercent, NGUONG_CANH_BAO_MAC_DINH);
    }

    /** Port VolumeTinhToanHelper.resolveProvinceVariant, ngưỡng cảnh báo truyền được. */
    public static String resolveProvinceVariant(long soTramThieu, long soTramThua, BigDecimal remaining, BigDecimal usedPercent,
                                                BigDecimal nguongCanhBao) {
        if (soTramThua > 0) {
            return "surplus";
        }
        if (soTramThieu > 0) {
            return "shortage";
        }
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            return "surplus";
        }
        if (usedPercent.compareTo(nguongCanhBao) >= 0) {
            return "warning";
        }
        return "normal";
    }

    public static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // ---- formula engine (port HangMucCalc* sibling classes) ----

    private enum RowType {ITEM, TASK}

    private enum Field {KL, DG, TT}

    private enum TokenType {NUMBER, OP, LPAREN, RPAREN, PERCENT, MA_REF, SELF_REF, SUM_REF}

    private static final class GridRow {
        int rowNumber;
        UUID id;
        UUID parentItemId;
        RowType type;
        String ma;
        BigDecimal khoiLuong;
        BigDecimal donGia;
        String congThucKhoiLuong;
        String congThucDonGia;
        String congThucThanhTien;
    }

    private static final class Grid {
        final List<GridRow> rows = new ArrayList<>();
        final Map<Integer, GridRow> byRowNumber = new HashMap<>();
        final Map<String, GridRow> byMa = new HashMap<>();
        final Map<UUID, List<GridRow>> childrenByItemId = new HashMap<>();

        void add(GridRow row) {
            rows.add(row);
            byRowNumber.put(row.rowNumber, row);
            if (row.ma != null) {
                byMa.put(normalizeMa(row.ma), row);
            }
            if (row.parentItemId != null) {
                childrenByItemId.computeIfAbsent(row.parentItemId, ignored -> new ArrayList<>()).add(row);
            }
        }
    }

    private static final class EvalContext {
        final Grid grid;
        final Map<String, BigDecimal> cache = new HashMap<>();
        final Set<String> stack = new HashSet<>();

        EvalContext(Grid grid) {
            this.grid = grid;
        }
    }

    private static final class Token {
        TokenType type;
        BigDecimal number;
        char op;
        String ma;
        Field field;

        static Token number(BigDecimal v) {
            Token t = new Token();
            t.type = TokenType.NUMBER;
            t.number = v;
            return t;
        }

        static Token op(char v) {
            Token t = new Token();
            t.type = TokenType.OP;
            t.op = v;
            return t;
        }

        static Token lparen() {
            Token t = new Token();
            t.type = TokenType.LPAREN;
            return t;
        }

        static Token rparen() {
            Token t = new Token();
            t.type = TokenType.RPAREN;
            return t;
        }

        static Token percent() {
            Token t = new Token();
            t.type = TokenType.PERCENT;
            return t;
        }

        static Token maRef(String ma, Field field) {
            Token t = new Token();
            t.type = TokenType.MA_REF;
            t.ma = ma;
            t.field = field;
            return t;
        }

        static Token selfRef(Field field) {
            Token t = new Token();
            t.type = TokenType.SELF_REF;
            t.field = field;
            return t;
        }

        static Token sumRef(Field field) {
            Token t = new Token();
            t.type = TokenType.SUM_REF;
            t.field = field;
            return t;
        }
    }

    private static Grid buildGrid(List<NhomRow> nhomRows, Map<UUID, List<ChiTietRow>> chiTietByNhom,
                                   Map<UUID, List<CongViecRow>> congViecByChiTiet,
                                   Map<UUID, BigDecimal> theoCongViec, Map<UUID, BigDecimal> theoChiTiet) {
        Grid grid = new Grid();
        int rowNumber = 0;
        for (NhomRow nhom : nhomRows) {
            for (ChiTietRow item : chiTietByNhom.getOrDefault(nhom.id(), List.of())) {
                rowNumber += 1;
                List<CongViecRow> tasks = congViecByChiTiet.getOrDefault(item.id(), List.of());
                boolean hasTasks = !tasks.isEmpty();
                GridRow itemRow = new GridRow();
                itemRow.rowNumber = rowNumber;
                itemRow.id = item.id();
                itemRow.type = RowType.ITEM;
                itemRow.ma = item.ma();
                itemRow.donGia = item.donGia();
                itemRow.congThucKhoiLuong = item.congThucKhoiLuong();
                itemRow.congThucDonGia = item.congThucDonGia();
                itemRow.congThucThanhTien = item.congThucThanhTien();
                if (!hasTasks) {
                    itemRow.khoiLuong = resolveKhoiLuong(item.khoiLuong(), item.congThucKhoiLuong(), theoChiTiet.get(item.id()));
                }
                grid.add(itemRow);

                for (CongViecRow task : tasks) {
                    rowNumber += 1;
                    GridRow taskRow = new GridRow();
                    taskRow.rowNumber = rowNumber;
                    taskRow.id = task.id();
                    taskRow.type = RowType.TASK;
                    taskRow.ma = task.ma();
                    taskRow.donGia = task.donGia();
                    taskRow.congThucKhoiLuong = task.congThucKhoiLuong();
                    taskRow.congThucDonGia = task.congThucDonGia();
                    taskRow.congThucThanhTien = task.congThucThanhTien();
                    taskRow.khoiLuong = resolveKhoiLuong(task.khoiLuong(), task.congThucKhoiLuong(), theoCongViec.get(task.id()));
                    taskRow.parentItemId = item.id();
                    grid.add(taskRow);
                }
            }
        }
        return grid;
    }

    private static BigDecimal resolveKhoiLuong(BigDecimal entityKl, String congThucKhoiLuong, BigDecimal sanLuongKl) {
        if (hasText(congThucKhoiLuong)) {
            return null;
        }
        if (entityKl != null && entityKl.signum() != 0) {
            return entityKl;
        }
        if (sanLuongKl != null) {
            return sanLuongKl;
        }
        return entityKl != null ? entityKl : BigDecimal.ZERO;
    }

    private static BigDecimal safeField(GridRow row, Field field, EvalContext ctx) {
        try {
            return getFieldValue(row.rowNumber, field, ctx);
        } catch (RuntimeException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal getFieldValue(int rowNumber, Field field, EvalContext ctx) {
        String key = rowNumber + ":" + field;
        if (ctx.cache.containsKey(key)) {
            return ctx.cache.get(key);
        }
        if (ctx.stack.contains(key)) {
            throw new IllegalStateException("Tham chiếu vòng tại dòng " + rowNumber + "." + field);
        }
        ctx.stack.add(key);
        try {
            GridRow row = ctx.grid.byRowNumber.get(rowNumber);
            if (row == null) {
                throw new IllegalStateException("Không có dòng " + rowNumber);
            }
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

    private static BigDecimal computeKhoiLuong(GridRow row, EvalContext ctx) {
        if (shouldAutoSum(row, ctx.grid, Field.KL)) {
            return sumChildren(row, Field.KL, ctx);
        }
        if (hasText(row.congThucKhoiLuong)) {
            return parseExpression(row.congThucKhoiLuong, ctx, row.rowNumber, Field.KL);
        }
        return nz(row.khoiLuong);
    }

    private static BigDecimal computeDonGia(GridRow row, EvalContext ctx) {
        if (hasText(row.congThucDonGia)) {
            return parseExpression(row.congThucDonGia, ctx, row.rowNumber, Field.DG);
        }
        if (shouldAutoSum(row, ctx.grid, Field.DG)) {
            return sumChildren(row, Field.DG, ctx);
        }
        return nz(row.donGia);
    }

    private static BigDecimal computeThanhTien(GridRow row, EvalContext ctx) {
        if (hasText(row.congThucThanhTien)) {
            return parseExpression(row.congThucThanhTien, ctx, row.rowNumber, Field.TT);
        }
        if (shouldAutoSum(row, ctx.grid, Field.TT)) {
            return sumChildren(row, Field.TT, ctx);
        }
        return getFieldValue(row.rowNumber, Field.KL, ctx).multiply(getFieldValue(row.rowNumber, Field.DG, ctx), MC);
    }

    private static boolean shouldAutoSum(GridRow row, Grid grid, Field field) {
        if (row.type != RowType.ITEM) {
            return false;
        }
        List<GridRow> children = grid.childrenByItemId.getOrDefault(row.id, List.of());
        if (children.isEmpty()) {
            return false;
        }
        if (field == Field.KL) {
            return true;
        }
        if (field == Field.DG) {
            if (hasText(row.congThucDonGia)) {
                return false;
            }
            return row.donGia == null || row.donGia.compareTo(BigDecimal.ZERO) == 0;
        }
        return !hasText(row.congThucThanhTien);
    }

    private static BigDecimal sumChildren(GridRow row, Field field, EvalContext ctx) {
        BigDecimal sum = BigDecimal.ZERO;
        for (GridRow child : ctx.grid.childrenByItemId.getOrDefault(row.id, List.of())) {
            sum = sum.add(getFieldValue(child.rowNumber, field, ctx));
        }
        return sum;
    }

    private static BigDecimal parseExpression(String formula, EvalContext ctx, int currentRow, Field defaultField) {
        String normalized = formula == null ? "" : formula.trim().replaceFirst("^=", "").trim();
        if (normalized.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Token> tokens = tokenize(normalized);
        Parser parser = new Parser(tokens, ctx, currentRow, defaultField);
        BigDecimal result = parser.parseAddSub();
        if (parser.index < tokens.size()) {
            throw new IllegalStateException("Biểu thức có phần thừa: " + formula);
        }
        return result;
    }

    private static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (Character.isWhitespace(ch)) {
                i += 1;
                continue;
            }
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                tokens.add(Token.op(ch));
                i += 1;
                continue;
            }
            if (ch == '(') {
                tokens.add(Token.lparen());
                i += 1;
                continue;
            }
            if (ch == ')') {
                tokens.add(Token.rparen());
                i += 1;
                continue;
            }
            if (ch == '%') {
                tokens.add(Token.percent());
                i += 1;
                continue;
            }
            if (Character.isDigit(ch) || ch == '.') {
                int start = i;
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i += 1;
                }
                tokens.add(Token.number(new BigDecimal(input.substring(start, i))));
                continue;
            }
            if (ch == '@') {
                int start = i + 1;
                i = start;
                while (i < input.length()) {
                    char c = input.charAt(i);
                    if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || isVietnameseLetter(c)) {
                        i += 1;
                        continue;
                    }
                    break;
                }
                tokens.add(parseAtRef(input.substring(start, i).trim()));
                continue;
            }
            throw new IllegalStateException("Ký tự không hợp lệ trong công thức: " + ch);
        }
        return tokens;
    }

    private static boolean isVietnameseLetter(char c) {
        return "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ"
                .indexOf(Character.toUpperCase(c)) >= 0
                || "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ".indexOf(c) >= 0;
    }

    private static Token parseAtRef(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        if (upper.equals("KL") || upper.equals("DG") || upper.equals("TT")
                || upper.equals("KHOILUONG") || upper.equals("DONGIA") || upper.equals("THANHTIEN")) {
            return Token.selfRef(parseField(upper));
        }
        if (upper.startsWith("SUM.")) {
            return Token.sumRef(parseField(upper.substring(4)));
        }
        int dot = raw.lastIndexOf('.');
        if (dot > 0) {
            return Token.maRef(normalizeMa(raw.substring(0, dot)), parseField(raw.substring(dot + 1)));
        }
        return Token.maRef(normalizeMa(raw), null);
    }

    private static Field parseField(String raw) {
        String upper = raw.trim().toUpperCase(Locale.ROOT).replace("Đ", "D").replace("Á", "A").replace("À", "A");
        return switch (upper) {
            case "KL", "KHOILUONG", "KHỐI LƯỢNG", "KHOI LUONG" -> Field.KL;
            case "DG", "DONGIA", "ĐG", "ĐƠN GIÁ", "DON GIA" -> Field.DG;
            case "TT", "THANHTIEN", "THÀNH TIỀN", "THANH TIEN" -> Field.TT;
            default -> throw new IllegalStateException("Trường không hợp lệ: " + raw);
        };
    }

    private static String normalizeMa(String ma) {
        return ma == null ? "" : ma.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class Parser {
        private final List<Token> tokens;
        private final EvalContext ctx;
        private final int currentRow;
        private final Field defaultField;
        int index;

        Parser(List<Token> tokens, EvalContext ctx, int currentRow, Field defaultField) {
            this.tokens = tokens;
            this.ctx = ctx;
            this.currentRow = currentRow;
            this.defaultField = defaultField;
        }

        private Token peek() {
            return index < tokens.size() ? tokens.get(index) : null;
        }

        private BigDecimal applyPercent(BigDecimal value) {
            if (peek() != null && peek().type == TokenType.PERCENT) {
                index += 1;
                return value.divide(HUNDRED, MC);
            }
            return value;
        }

        private BigDecimal parsePrimary() {
            Token token = peek();
            if (token == null) {
                throw new IllegalStateException("Biểu thức chưa hoàn chỉnh");
            }
            if (token.type == TokenType.NUMBER) {
                index += 1;
                return applyPercent(token.number);
            }
            if (token.type == TokenType.SUM_REF) {
                index += 1;
                GridRow row = ctx.grid.byRowNumber.get(currentRow);
                if (row == null) {
                    throw new IllegalStateException("@SUM cần dòng hiện tại");
                }
                return applyPercent(sumChildren(row, token.field, ctx));
            }
            if (token.type == TokenType.MA_REF) {
                index += 1;
                GridRow row = ctx.grid.byMa.get(normalizeMa(token.ma));
                if (row == null) {
                    throw new IllegalStateException("Không tìm thấy mã: " + token.ma);
                }
                Field field = token.field != null ? token.field : defaultField;
                return applyPercent(getFieldValue(row.rowNumber, field, ctx));
            }
            if (token.type == TokenType.SELF_REF) {
                index += 1;
                return applyPercent(getFieldValue(currentRow, token.field, ctx));
            }
            if (token.type == TokenType.LPAREN) {
                index += 1;
                BigDecimal value = parseAddSub();
                if (peek() == null || peek().type != TokenType.RPAREN) {
                    throw new IllegalStateException("Thiếu dấu )");
                }
                index += 1;
                return applyPercent(value);
            }
            if (token.type == TokenType.OP && token.op == '-') {
                index += 1;
                return parsePrimary().negate();
            }
            if (token.type == TokenType.OP && token.op == '+') {
                index += 1;
                return parsePrimary();
            }
            throw new IllegalStateException("Biểu thức không hợp lệ");
        }

        private BigDecimal parseMulDiv() {
            BigDecimal value = parsePrimary();
            while (true) {
                Token token = peek();
                if (token == null || token.type != TokenType.OP || (token.op != '*' && token.op != '/')) {
                    break;
                }
                char op = token.op;
                index += 1;
                BigDecimal right = parsePrimary();
                if (op == '*') {
                    value = value.multiply(right, MC);
                } else {
                    if (right.compareTo(BigDecimal.ZERO) == 0) {
                        throw new IllegalStateException("Không thể chia cho 0");
                    }
                    value = value.divide(right, MC);
                }
            }
            return value;
        }

        BigDecimal parseAddSub() {
            BigDecimal value = parseMulDiv();
            while (true) {
                Token token = peek();
                if (token == null || token.type != TokenType.OP || (token.op != '+' && token.op != '-')) {
                    break;
                }
                char op = token.op;
                index += 1;
                BigDecimal right = parseMulDiv();
                value = op == '+' ? value.add(right) : value.subtract(right);
            }
            return value;
        }
    }
}
