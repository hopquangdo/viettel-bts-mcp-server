package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.openapi.QuyenHanMa;

import java.util.Set;

public final class ContractorReadPermissions {

    private static final Set<String> ALLOWED = Set.of(
            QuyenHanMa.XEM_NHIEM_VU_NHA_THAU,
            QuyenHanMa.QUAN_LY_HOP_DONG,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_TRANG_THAI,
            QuyenHanMa.QUAN_LY_HOP_DONG_THUOC_TINH,
            QuyenHanMa.QUAN_LY_HANG_MUC_NHOM,
            QuyenHanMa.QUAN_LY_HANG_MUC_CHI_TIET,
            QuyenHanMa.QUAN_LY_HANG_MUC_CONG_VIEC,
            QuyenHanMa.QUAN_LY_SAN_LUONG,
            QuyenHanMa.QUAN_LY_PHAN_CONG,
            QuyenHanMa.QUAN_LY_KHU_VUC,
            QuyenHanMa.QUAN_LY_TINH_THANH,
            QuyenHanMa.QUAN_LY_LOAI_HOP_DONG,
            QuyenHanMa.QUAN_LY_KIEU_HOP_DONG,
            QuyenHanMa.QUAN_LY_TRANG_THAI_HOP_DONG,
            QuyenHanMa.QUAN_LY_THUOC_TINH,
            QuyenHanMa.QUAN_LY_HOP_DONG_TRANG_THAI,
            QuyenHanMa.QUAN_LY_CHU_DAU_TU);

    private ContractorReadPermissions() {
    }

    public static boolean allowsRead(String quyenHanMa) {
        return quyenHanMa != null && ALLOWED.contains(quyenHanMa.trim());
    }
}
