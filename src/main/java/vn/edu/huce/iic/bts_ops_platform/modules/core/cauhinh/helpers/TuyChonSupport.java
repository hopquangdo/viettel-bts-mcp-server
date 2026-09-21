package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TuyChonSupport {

    private TuyChonSupport() {
    }

    public static boolean requiresOptions(String kieuDuLieuId) {
        if (kieuDuLieuId == null || kieuDuLieuId.isBlank()) {
            return false;
        }
        String normalized = kieuDuLieuId.trim().toLowerCase(Locale.ROOT);
        return "select".equals(normalized) || "checkbox".equals(normalized);
    }

    public static List<String> normalize(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String item : raw) {
            if (item == null) {
                continue;
            }
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                seen.add(trimmed);
            }
        }
        return new ArrayList<>(seen);
    }

    public static List<String> normalizeForType(String kieuDuLieuId, List<String> raw) {
        List<String> normalized = normalize(raw);
        if (!requiresOptions(kieuDuLieuId)) {
            return List.of();
        }
        if (normalized.isEmpty()) {
            throw new AppException(
                    CauHinhErrorCode.THUOC_TINH_TUY_CHON_REQUIRED,
                    "Kiểu Select/Checkbox phải có ít nhất một giá trị lựa chọn");
        }
        return normalized;
    }
}
