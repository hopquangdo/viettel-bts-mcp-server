package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Vai trò có toàn quyền — bỏ qua ma trận {@code phan_quyen_quyen_han}, nạp mọi quyền hạn đang hoạt động.
 */
public final class FullAccessRoleCodes {

    private static final Set<String> MA_CODES = Set.of(
            "SUPERADMIN",
            "SUPER_ADMIN");

    private FullAccessRoleCodes() {
    }

    public static boolean isFullAccess(String quyenMa) {
        return isFullAccess(quyenMa, null);
    }

    /** Khớp mã vai trò hoặc tên hiển thị (Giám đốc, Trung tâm khu vực, …). */
    public static boolean isFullAccess(String quyenMa, String quyenTen) {
        if (quyenMa != null && !quyenMa.isBlank()) {
            if (MA_CODES.contains(quyenMa.trim().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return matchesFullAccessTen(quyenTen);
    }

    public static boolean isFullAccessRoleAuthority(String authority) {
        if (authority == null) {
            return false;
        }
        for (String code : MA_CODES) {
            if (AuthorityPrefix.roleQuyen(code).equals(authority)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesFullAccessTen(String quyenTen) {
        if (quyenTen == null || quyenTen.isBlank()) {
            return false;
        }
        String normalized = normalizeTen(quyenTen);
        if ("giam doc".equals(normalized)) {
            return true;
        }
        return normalized.contains("trung tam khu vuc");
    }

    private static String normalizeTen(String ten) {
        String lower = ten.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
