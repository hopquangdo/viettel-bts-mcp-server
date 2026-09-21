package vn.edu.huce.iic.bts_ops_platform.support;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Ép kiểu ngày từ Object[] của native query (Timestamp/Date/Instant/LocalDate) — dùng chung cho mọi tool group-by-thang bằng native SQL. */
public final class DateConversionUtil {

    private DateConversionUtil() {
    }

    public static LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        throw new IllegalStateException("Unsupported date type: " + value.getClass());
    }
}
