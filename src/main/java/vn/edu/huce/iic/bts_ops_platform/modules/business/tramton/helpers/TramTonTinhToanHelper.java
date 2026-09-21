package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Phân loại điều kiện doanh thu / tuổi tồn cho module trạm tồn. */
public final class TramTonTinhToanHelper {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final int QUA_HAN_NGAY = 90;

    public static final String TAB_ALL = "all";
    public static final String TAB_CHO_QT = "cho_qt";
    public static final String TAB_PHAP_LY = "phap_ly";
    public static final String TAB_VUONG = "vuong";
    public static final String TAB_HTTC = "httc";
    public static final String TAB_THIEU = "thieu";
    public static final String TAB_HAN = "han";

    public static final String ST_DU_DK = "du_dk_dt";
    public static final String ST_CHO_QT = "cho_qt";
    public static final String ST_VUONG = "vuong";
    public static final String ST_PHAP_LY = "thieu_phap_ly";
    public static final String ST_THIEU_2 = "thieu_2_dk";
    public static final String ST_THIEU_HTTC = "thieu_httc";
    public static final String ST_QUA_HAN = "qua_han";

    public static final String DK_HTTC = "HTTC";
    public static final String DK_VUONG = "Đang vướng";
    public static final String DK_PHAP_LY = "Pháp lý";

    private TramTonTinhToanHelper() {
    }

    public static boolean isQuyetToan(String trangThaiMa, BigDecimal quyetToanThuc) {
        if (quyetToanThuc != null) {
            return true;
        }
        if (trangThaiMa == null || trangThaiMa.isBlank()) {
            return false;
        }
        String ma = trangThaiMa.trim().toUpperCase(Locale.ROOT);
        return "QT".equals(ma)
                || ma.contains("QUYET TOAN")
                || ma.contains("QUYẾT TOÁN");
    }

    public static boolean isHtThiCong(String trangThaiMa) {
        return HopDongDanhSachTienDoHelper.isCompletedStatus(trangThaiMa);
    }

    public static boolean hasPhapLy(Short trangThaiPhapLy) {
        return trangThaiPhapLy != null && trangThaiPhapLy == 2;
    }

    public static List<String> buildThieuDieuKien(boolean ht, boolean noOpenVm, boolean phapLy) {
        List<String> missing = new ArrayList<>();
        if (!ht) {
            missing.add(DK_HTTC);
        }
        if (!noOpenVm) {
            missing.add(DK_VUONG);
        }
        if (!phapLy) {
            missing.add(DK_PHAP_LY);
        }
        return missing;
    }

    public static int normalizeQuaHanNgay(Integer quaHanNgay) {
        if (quaHanNgay == null || quaHanNgay < 0) {
            return QUA_HAN_NGAY;
        }
        return Math.min(quaHanNgay, 3650);
    }

    public static LocalDate resolveNgayBaoCao(LocalDate ngayBaoCao) {
        return ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
    }

    public static String resolveTrangThai(List<String> thieuDieuKien, long soNgayTon) {
        return resolveTrangThai(thieuDieuKien, soNgayTon, QUA_HAN_NGAY);
    }

    public static String resolveTrangThai(List<String> thieuDieuKien, long soNgayTon, int quaHanNgay) {
        int threshold = normalizeQuaHanNgay(quaHanNgay);
        if (thieuDieuKien == null || thieuDieuKien.isEmpty()) {
            return soNgayTon >= threshold ? ST_QUA_HAN : ST_CHO_QT;
        }
        if (thieuDieuKien.size() >= 2) {
            return ST_THIEU_2;
        }
        String only = thieuDieuKien.get(0);
        if (DK_VUONG.equals(only)) {
            return ST_VUONG;
        }
        if (DK_PHAP_LY.equals(only)) {
            return ST_PHAP_LY;
        }
        return ST_THIEU_HTTC;
    }

    public static boolean matchesTab(String tab, String trangThai, List<String> thieuDieuKien) {
        String t = tab == null || tab.isBlank() ? TAB_ALL : tab.trim().toLowerCase(Locale.ROOT);
        return switch (t) {
            case TAB_CHO_QT -> ST_CHO_QT.equals(trangThai) || ST_DU_DK.equals(trangThai);
            case TAB_HAN -> ST_QUA_HAN.equals(trangThai);
            case TAB_VUONG -> ST_VUONG.equals(trangThai)
                    || (thieuDieuKien != null && thieuDieuKien.contains(DK_VUONG));
            case TAB_PHAP_LY -> ST_PHAP_LY.equals(trangThai)
                    || (thieuDieuKien != null && thieuDieuKien.contains(DK_PHAP_LY));
            case TAB_HTTC -> ST_THIEU_HTTC.equals(trangThai)
                    || (thieuDieuKien != null && thieuDieuKien.contains(DK_HTTC));
            case TAB_THIEU -> ST_THIEU_2.equals(trangThai);
            default -> true;
        };
    }

    public static String reasonBucketId(List<String> thieuDieuKien) {
        if (thieuDieuKien == null || thieuDieuKien.isEmpty()) {
            return null;
        }
        if (thieuDieuKien.size() >= 2) {
            return "thieu_2";
        }
        String only = thieuDieuKien.get(0);
        if (DK_HTTC.equals(only)) {
            return "httc";
        }
        if (DK_VUONG.equals(only)) {
            return "vuong";
        }
        if (DK_PHAP_LY.equals(only)) {
            return "phap_ly";
        }
        return null;
    }

    public static String agingBucketId(long soNgayTon) {
        if (soNgayTon < 7) {
            return "lt_1w";
        }
        if (soNgayTon < 14) {
            return "1_2w";
        }
        if (soNgayTon < 28) {
            return "2_4w";
        }
        if (soNgayTon < 90) {
            return "1_3m";
        }
        return "gt_3m";
    }

    public static LocalDate resolveNgayHtTc(LocalDate ngayHtTc, Instant ngayCapNhat, boolean ht) {
        if (ngayHtTc != null) {
            return ngayHtTc;
        }
        if (ht && ngayCapNhat != null) {
            return ngayCapNhat.atZone(ZONE).toLocalDate();
        }
        return null;
    }

    public static long soNgayTon(LocalDate ngayHtTc, LocalDate today) {
        if (ngayHtTc == null || today == null) {
            return 1L;
        }
        long days = ChronoUnit.DAYS.between(ngayHtTc, today);
        return Math.max(0L, days);
    }

    public static BigDecimal nz(BigDecimal value) {
        return VolumeTinhToanHelper.nz(value);
    }

    public static double percent(long part, long total) {
        if (total <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf(part * 10000L / total)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
