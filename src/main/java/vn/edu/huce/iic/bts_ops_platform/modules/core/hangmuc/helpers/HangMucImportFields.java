package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import java.util.Locale;
import java.util.UUID;

/**
 * Hằng số dùng chung cho import Excel Hạng mục thi công.
 * Tên thuộc tính cố định — khớp cấu hình Đối tượng quản lý / Excel mapping.
 */
public final class HangMucImportFields {

    public static final UUID DOI_TUONG_ID =
            UUID.fromString("d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2");

    public static final int MAX_MA_LENGTH = 50;
    public static final String DEFAULT_GROUP_CACHE_KEY = "";
    public static final String DEFAULT_NHOM_MA_SLUG = "DEFAULT";
    public static final String DEFAULT_CHI_TIET_DS_SUFFIX = "_DS";
    public static final String DEFAULT_CHI_TIET_TEN = "Danh sách công việc";
    public static final String DEFAULT_GROUP_MO_TA = "Nhóm hạng mục";
    public static final String GROUP_IMPORT_MO_TA = "Nhóm hạng mục import từ Excel";
    public static final String MA_PREFIX_HM = "HM";
    public static final String MA_PREFIX_CV = "CV";
    public static final String MA_PREFIX_HD = "HM_HD";
    public static final String MA_PREFIX_DS = "DS";
    public static final String TRANG_THAI_CHI_TIET_ACTIVE = "active";
    public static final String TRANG_THAI_CONG_VIEC_PENDING = "pending";

    public static final String NHOM_HANG_MUC = "Nhóm hạng mục";
    public static final String HANG_MUC = "Hạng mục";
    public static final String CONG_TAC = "Công tác";
    public static final String DON_GIA = "Đơn giá";
    public static final String KHOI_LUONG = "Khối lượng";
    public static final String DON_VI = "Đơn vị";
    public static final String VI_TRI_THI_CONG = "Vị trí thi công";
    public static final String MA_CONG_TAC = "Mã công tác";
    public static final String MA_IMPORT = "Mã thuộc tính";
    public static final String STT = "STT";

    private HangMucImportFields() {
    }

    public static String nameFor(HangMucImportAttributeRole role) {
        return switch (role) {
            case NHOM_HANG_MUC -> NHOM_HANG_MUC;
            case HANG_MUC -> HANG_MUC;
            case CONG_TAC -> CONG_TAC;
            case DON_GIA -> DON_GIA;
            case KHOI_LUONG -> KHOI_LUONG;
            case DON_VI -> DON_VI;
            case VI_TRI_THI_CONG -> VI_TRI_THI_CONG;
            case MA_CONG_TAC -> MA_CONG_TAC;
            case MA_IMPORT -> MA_IMPORT;
            case STT -> STT;
        };
    }

    public static boolean matchesName(String fieldName, String canonicalName) {
        if (fieldName == null || fieldName.isBlank() || canonicalName == null) {
            return false;
        }
        return fieldName.trim().equalsIgnoreCase(canonicalName.trim());
    }

    public static boolean matchesRole(String fieldName, HangMucImportAttributeRole role) {
        return matchesName(fieldName, nameFor(role));
    }

    /** Cấp phân cấp khi parse Excel: 1 = nhóm, 2 = hạng mục. */
    public static Short hierarchyCap(String fieldName) {
        if (matchesRole(fieldName, HangMucImportAttributeRole.NHOM_HANG_MUC)) {
            return (short) 1;
        }
        if (matchesRole(fieldName, HangMucImportAttributeRole.HANG_MUC)) {
            return (short) 2;
        }
        return null;
    }

    public static String maKey(String ma) {
        return ma.toLowerCase(Locale.ROOT);
    }
}
