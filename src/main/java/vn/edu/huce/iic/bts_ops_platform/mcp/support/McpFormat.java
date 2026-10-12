package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Định dạng số liệu trả cho LLM đọc theo kiểu vi-VN: dấu phẩy thập phân, dấu chấm phân cách nghìn.
 * Tiền tự đổi đơn vị tỷ/triệu/nghìn đồng ("1,25 tỷ đồng"), phần trăm "82,5%", ngày "30/09/2026",
 * kỳ "01/01/2026 – 30/09/2026". Chỉ dùng cho DTO trả ra của tool, không dùng cho dữ liệu nội bộ cần tính toán.
 */
public final class McpFormat {

    private static final BigDecimal TY = new BigDecimal("1000000000");
    private static final BigDecimal TRIEU = new BigDecimal("1000000");
    private static final BigDecimal NGHIN = new BigDecimal("1000");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private McpFormat() {
    }

    /** Số kiểu vi-VN, tối đa {@code soLe} chữ số thập phân, bỏ số 0 thừa: 1234.5 -> "1.234,5". */
    public static String so(BigDecimal value, int soLe) {
        if (value == null) {
            return null;
        }
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setDecimalSeparator(',');
        sym.setGroupingSeparator('.');
        sym.setMinusSign('-');
        DecimalFormat df = new DecimalFormat(soLe > 0 ? "#,##0." + "#".repeat(soLe) : "#,##0", sym);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

    public static String so(long value) {
        return so(BigDecimal.valueOf(value), 0);
    }

    /** Tiền VNĐ: >= 1 tỷ -> "x tỷ đồng", >= 1 triệu -> "x triệu đồng", >= 1 nghìn -> "x nghìn đồng", còn lại "x đồng". */
    public static String tien(BigDecimal dong) {
        if (dong == null) {
            return null;
        }
        BigDecimal abs = dong.abs();
        if (abs.compareTo(TY) >= 0) {
            return so(dong.divide(TY, 4, RoundingMode.HALF_UP), 2) + " tỷ đồng";
        }
        if (abs.compareTo(TRIEU) >= 0) {
            return so(dong.divide(TRIEU, 4, RoundingMode.HALF_UP), 2) + " triệu đồng";
        }
        if (abs.compareTo(NGHIN) >= 0) {
            return so(dong.divide(NGHIN, 4, RoundingMode.HALF_UP), 2) + " nghìn đồng";
        }
        return so(dong, 0) + " đồng";
    }

    /**
     * Hàm chung cho mọi kiểu số tiền VNĐ (BigDecimal, Long, Integer, Double...): null -> null, còn lại như {@link #tien(BigDecimal)}.
     * DTO trả cho tool nên gắn {@link Tien} vào trường thay vì gọi tay.
     */
    public static String tien(Number dong) {
        if (dong == null) {
            return null;
        }
        if (dong instanceof BigDecimal bd) {
            return tien(bd);
        }
        if (dong instanceof Double || dong instanceof Float) {
            return tien(BigDecimal.valueOf(dong.doubleValue()));
        }
        return tien(new BigDecimal(dong.toString()));
    }

    /** Số liệu đang tính bằng tỷ đồng (module nguồn việc) -> chuỗi tiền. */
    public static String tienTuTy(double ty) {
        return tien(BigDecimal.valueOf(ty).multiply(TY));
    }

    /** Phần trăm đã nhân 100: 82.5 -> "82,5%". */
    public static String phanTram(BigDecimal value) {
        return value == null ? null : so(value, 1) + "%";
    }

    public static String phanTram(double value) {
        return phanTram(BigDecimal.valueOf(value));
    }

    public static String ngay(LocalDate date) {
        return date == null ? null : date.format(NGAY);
    }

    /** Ngày ISO (yyyy-MM-dd) -> "dd/MM/yyyy"; chuỗi rỗng/không parse được thì trả nguyên. */
    public static String ngay(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return ngay(LocalDate.parse(iso.trim()));
        } catch (RuntimeException e) {
            return iso;
        }
    }

    /** "01/01/2026 – 30/09/2026". */
    public static String ky(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null && denNgay == null) {
            return null;
        }
        return (tuNgay != null ? ngay(tuNgay) : "…") + " – " + (denNgay != null ? ngay(denNgay) : "…");
    }

    public static String ky(String tuNgayIso, String denNgayIso) {
        String tu = ngay(tuNgayIso);
        String den = ngay(denNgayIso);
        if (tu == null && den == null) {
            return null;
        }
        return (tu != null ? tu : "…") + " – " + (den != null ? den : "…");
    }
}
