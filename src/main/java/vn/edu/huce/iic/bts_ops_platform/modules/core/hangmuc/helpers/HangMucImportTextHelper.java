package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import java.util.Locale;

public final class HangMucImportTextHelper {

    private HangMucImportTextHelper() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String normalizeKey(String text) {
        if (isBlank(text)) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static String hangMucCacheKey(String tenNhomHangMuc, String tenHangMuc) {
        return normalizeKey(tenNhomHangMuc) + "::" + normalizeKey(tenHangMuc);
    }
}
