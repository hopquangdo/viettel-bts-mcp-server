package vn.edu.huce.iic.bts_ops_platform.mcp.common.openapi;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.security.RequiresPermission;

/**
 * Mã quyền hạn (bảng {@code quyen_han.ma}) gắn với từng entity API.
 * Dùng trong {@link RequiresPermission}
 * và hiển thị trên Swagger UI.
 */
public final class QuyenHanMa {

    private QuyenHanMa() {
    }

    // Xác thực & người dùng
    public static final String QUAN_LY_NGUOI_DUNG = "QUAN_LY_NGUOI_DUNG";
    public static final String XEM_NHIEM_VU_NHA_THAU = "XEM_NHIEM_VU_NHA_THAU";

    // Phân quyền
    public static final String QUAN_LY_QUYEN = "QUAN_LY_QUYEN";
    public static final String QUAN_LY_QUYEN_HAN = "QUAN_LY_QUYEN_HAN";
    public static final String QUAN_LY_PHAN_QUYEN = "QUAN_LY_PHAN_QUYEN";

    // Thư viện
    public static final String QUAN_LY_KHU_VUC = "QUAN_LY_KHU_VUC";
    public static final String QUAN_LY_TINH_THANH = "QUAN_LY_TINH_THANH";

    // Cấu hình hợp đồng
    public static final String QUAN_LY_LOAI_HOP_DONG = "QUAN_LY_LOAI_HOP_DONG";
    public static final String QUAN_LY_KIEU_HOP_DONG = "QUAN_LY_KIEU_HOP_DONG";
    public static final String QUAN_LY_DOI_TUONG_QUAN_LY = "QUAN_LY_DOI_TUONG_QUAN_LY";
    public static final String QUAN_LY_TRANG_THAI_HOP_DONG = "QUAN_LY_TRANG_THAI_HOP_DONG";
    public static final String QUAN_LY_KIEU_DU_LIEU = "QUAN_LY_KIEU_DU_LIEU";
    public static final String QUAN_LY_THUOC_TINH = "QUAN_LY_THUOC_TINH";
    public static final String QUAN_LY_HOP_DONG_TRANG_THAI = "QUAN_LY_HOP_DONG_TRANG_THAI";
    public static final String QUAN_LY_EXCEL_MAPPING = "QUAN_LY_EXCEL_MAPPING";

    // Hợp đồng
    public static final String QUAN_LY_HOP_DONG = "QUAN_LY_HOP_DONG";
    public static final String QUAN_LY_CHU_DAU_TU = "QUAN_LY_CHU_DAU_TU";
    public static final String QUAN_LY_HOP_DONG_THUOC_TINH = "QUAN_LY_HOP_DONG_THUOC_TINH";
    public static final String QUAN_LY_HOP_DONG_TEP_DINH_KEM = "QUAN_LY_HOP_DONG_TEP_DINH_KEM";
    public static final String QUAN_LY_HOP_DONG_NHOM_UU_TIEN = "QUAN_LY_HOP_DONG_NHOM_UU_TIEN";
    public static final String QUAN_LY_HOP_DONG_DOI_TUONG = "QUAN_LY_HOP_DONG_DOI_TUONG";
    public static final String QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI = "QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI";
    public static final String QUAN_LY_HOP_DONG_DOI_TUONG_TRANG_THAI = "QUAN_LY_HOP_DONG_DOI_TUONG_TRANG_THAI";

    // Hạng mục & sản lượng
    public static final String QUAN_LY_HANG_MUC_NHOM = "QUAN_LY_HANG_MUC_NHOM";
    public static final String QUAN_LY_HANG_MUC_CHI_TIET = "QUAN_LY_HANG_MUC_CHI_TIET";
    public static final String QUAN_LY_HANG_MUC_CONG_VIEC = "QUAN_LY_HANG_MUC_CONG_VIEC";
    public static final String QUAN_LY_SAN_LUONG = "QUAN_LY_SAN_LUONG";
    /** Ghi nhận đạt/không đạt nghiệm thu hạng mục sản lượng — tách khỏi QUAN_LY_SAN_LUONG. */
    public static final String NGHIEM_THU_SAN_LUONG = "NGHIEM_THU_SAN_LUONG";

    // Nghiệp vụ & tệp
    /** Duyệt / từ chối biên bản (nghiệm thu + phát sinh + ghi ngày ký phụ lục) — tách khỏi
     * QUAN_LY_HOP_DONG_DOI_TUONG để chỉ cấp có thẩm quyền (trưởng phòng trở lên) duyệt. */
    public static final String PHE_DUYET_BIEN_BAN = "PHE_DUYET_BIEN_BAN";
    /** Sửa số liệu đã trình ký/bàn giao — hiện mới seed vào ma trận, sẽ gắn vào endpoint sửa số
     * liệu đã duyệt khi chức năng đó được xây. */
    public static final String SUA_SO_LIEU_DA_DUYET = "SUA_SO_LIEU_DA_DUYET";
    public static final String QUAN_LY_LUU_TRU = "QUAN_LY_LUU_TRU";
    public static final String QUAN_LY_PHAN_CONG = "QUAN_LY_PHAN_CONG";
    public static final String QUAN_LY_LUONG_HOP_DONG = "QUAN_LY_LUONG_HOP_DONG";
    public static final String QUAN_LY_TRO_LY_AI = "QUAN_LY_TRO_LY_AI";
    public static final String QUAN_LY_NGUON_LUC = "QUAN_LY_NGUON_LUC";
    public static final String QUAN_LY_AUDIT_LOG = "QUAN_LY_AUDIT_LOG";
    public static final String QUAN_LY_VUONG_MAC = "QUAN_LY_VUONG_MAC";
    public static final String QUAN_LY_TEP_DINH_KEM = "QUAN_LY_TEP_DINH_KEM";
}
