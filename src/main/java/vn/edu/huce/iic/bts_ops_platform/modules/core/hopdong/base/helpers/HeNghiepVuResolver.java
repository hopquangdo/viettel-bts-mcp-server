package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;

/** Suy luận nhóm nghiệp vụ từ LoaiHopDong.heNghiepVu (ưu tiên) hoặc mã/tên loại HĐ. */
public final class HeNghiepVuResolver {

    public static final String THI_CONG = "thi_cong";
    public static final String TU_VAN_THIET_KE = "tu_van_thiet_ke";

    private HeNghiepVuResolver() {
    }

    public static boolean isTuVanThietKe(LoaiHopDong loaiHopDong) {
        if (loaiHopDong == null) {
            return false;
        }
        return isTuVanThietKe(loaiHopDong.getHeNghiepVu(), loaiHopDong.getMa(), loaiHopDong.getTen());
    }

    public static boolean isTuVanThietKe(String heNghiepVu, String loaiMa, String loaiTen) {
        if (TU_VAN_THIET_KE.equals(heNghiepVu)) {
            return true;
        }
        if (heNghiepVu != null && !heNghiepVu.isBlank() && !THI_CONG.equals(heNghiepVu)) {
            return false;
        }
        return inferTuVanFromLabel(loaiMa, loaiTen);
    }

    private static boolean inferTuVanFromLabel(String ma, String ten) {
        String normalizedMa = normalize(ma);
        String normalizedTen = normalize(ten);
        if (normalizedMa.equals("tvtk")
                || normalizedMa.equals("kd")
                || normalizedMa.contains("kiemdinh")
                || normalizedMa.contains("tu_van")
                || normalizedMa.contains("khao_sat")) {
            return true;
        }
        if (normalizedTen.contains("tu van thiet ke")
                || normalizedTen.contains("kiem dinh")
                || normalizedTen.contains("khao sat thiet ke")) {
            return true;
        }
        return normalizedTen.contains("tu van") && normalizedTen.contains("thiet ke");
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("\\p{M}", "");
    }
}
