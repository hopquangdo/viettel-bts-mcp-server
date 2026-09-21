package vn.edu.huce.iic.bts_ops_platform.common.util;

import java.text.Normalizer;
import java.util.Locale;

/** Tiện ích chuẩn hóa chuỗi dùng chung (bỏ dấu tiếng Việt, chuẩn hóa để so sánh). */
public final class TextUtils {

    private TextUtils() {
    }

    /** Bỏ dấu tiếng Việt, giữ nguyên hoa/thường. Trả về "" nếu null. */
    public static String stripAccents(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    /** Bỏ dấu + trim + lowercase, phục vụ so khớp không phân biệt hoa thường/dấu. */
    public static String normalizeLower(String value) {
        if (value == null) {
            return "";
        }
        return stripAccents(value.trim().toLowerCase(Locale.ROOT));
    }
}
