package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Null-safe cho số + tính tỷ lệ % (done/total) — dùng chung cho mọi tool phải xử lý cột nullable từ projection. */
public final class NumberUtil {

    private NumberUtil() {
    }

    public static int nz(Integer value) {
        return value != null ? value : 0;
    }

    public static long nz(Long value) {
        return value != null ? value : 0L;
    }

    public static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    /** Ép Object (BigDecimal/Number/null) từ native query thành BigDecimal — trả ZERO nếu null. */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) value).doubleValue());
    }

    /** (done/total)*100, làm tròn scale chữ số thập phân. Trả về ZERO nếu total <= 0. */
    public static BigDecimal percent(int done, int total, int scale) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(done)
                .divide(BigDecimal.valueOf(total), scale + 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, RoundingMode.HALF_UP);
    }
}
