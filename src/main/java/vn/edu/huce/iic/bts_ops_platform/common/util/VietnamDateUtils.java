package vn.edu.huce.iic.bts_ops_platform.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class VietnamDateUtils {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private VietnamDateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE).toLocalDate();
    }
}
