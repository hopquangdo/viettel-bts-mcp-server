package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.helpers;

import java.util.Locale;
import java.util.Set;

/** Kiểu vướng mắc và quy định có được bổ sung sản lượng hay không. */
public final class VuongMacKieuHelper {

    public static final String GIAI_PHONG_MAT_BANG = "giai_phong_mat_bang";
    public static final String THIET_KE = "thiet_ke";
    public static final String THU_TUC_PHAP_LY = "thu_tuc_phap_ly";
    public static final String NGUON_VAT_LIEU = "nguon_vat_lieu";
    public static final String THI_CONG = "thi_cong";

    /** Kiểu vướng mắc đang mở sẽ chặn bổ sung sản lượng. */
    public static final Set<String> BLOCKING_KIEUS = Set.of(
            GIAI_PHONG_MAT_BANG, THIET_KE, THU_TUC_PHAP_LY);

    /** Kiểu vướng mắc đang mở vẫn cho phép bổ sung sản lượng. */
    public static final Set<String> ALLOWING_KIEUS = Set.of(
            NGUON_VAT_LIEU, THI_CONG);

    private VuongMacKieuHelper() {
    }

    public static String normalize(String kieu) {
        if (kieu == null || kieu.isBlank()) {
            return GIAI_PHONG_MAT_BANG;
        }
        String normalized = kieu.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case GIAI_PHONG_MAT_BANG, THIET_KE, THU_TUC_PHAP_LY, NGUON_VAT_LIEU, THI_CONG -> normalized;
            default -> GIAI_PHONG_MAT_BANG;
        };
    }

    public static boolean blocksOutput(String kieu) {
        return BLOCKING_KIEUS.contains(normalize(kieu));
    }

    public static boolean allowsOutput(String kieu) {
        return ALLOWING_KIEUS.contains(normalize(kieu));
    }

    /**
     * Suy luận kiểu vướng mắc từ dữ liệu legacy (giai đoạn + mô tả) — dùng backfill bản ghi cũ.
     * Ưu tiên từ khóa trong mô tả, sau đó map theo giai đoạn.
     */
    public static String inferFromLegacyData(String giaiDoan, String moTa, String moTaDayDu) {
        String combined = ((moTa != null ? moTa : "") + " " + (moTaDayDu != null ? moTaDayDu : ""))
                .toLowerCase(Locale.ROOT);
        String phase = giaiDoan != null ? giaiDoan.toLowerCase(Locale.ROOT) : "";

        if (containsAny(combined,
                "vật liệu", "vat lieu", "nguon vat", "nguồn vật", "cung cấp vật", "cung cap vat")) {
            return NGUON_VAT_LIEU;
        }
        if (containsAny(combined,
                "pháp lý", "phap ly", "giấy phép", "giay phep", "hồ sơ pháp", "ho so phap", "quy hoạch")) {
            return THU_TUC_PHAP_LY;
        }
        if (containsAny(combined,
                "mặt bằng", "mat bang", "giải phóng", "giai phong", "đền bù", "den bu", "thu hồi", "thu hoi")) {
            return GIAI_PHONG_MAT_BANG;
        }
        if (containsAny(combined,
                "thiết kế", "thiet ke", "bản vẽ", "ban ve", "hồ sơ thiết kế", "ho so thiet ke")) {
            return THIET_KE;
        }
        if (containsAny(combined,
                "thi công", "thi cong", "lắp đặt", "lap dat", "xây dựng", "xay dung", "thực hiện thi công")) {
            return THI_CONG;
        }

        if (containsAny(phase, "thiết kế", "thiet ke", "dự toán", "du toan")) {
            return THIET_KE;
        }
        if (containsAny(phase, "thi công", "thi cong", "nghiệm thu", "nghiem thu")) {
            return THI_CONG;
        }
        if (containsAny(phase, "khảo sát", "khao sat")) {
            return GIAI_PHONG_MAT_BANG;
        }

        return GIAI_PHONG_MAT_BANG;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
