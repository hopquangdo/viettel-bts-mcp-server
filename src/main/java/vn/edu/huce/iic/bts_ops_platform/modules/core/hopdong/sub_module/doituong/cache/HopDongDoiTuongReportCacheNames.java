package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache;

public final class HopDongDoiTuongReportCacheNames {

    /**
     * Kết quả các báo cáo/thống kê đọc nhiều (breakdown khu vực, thiếu cập nhật, sản lượng bất
     * thường, tốc độ hoàn thành) — key = chữ ký tham số của từng report. Evict theo toàn bộ tên
     * cache (không theo key riêng lẻ) vì các report này đọc chéo dữ liệu nhiều bảng, không rẻ để
     * xác định chính xác entry nào bị ảnh hưởng bởi 1 thay đổi cụ thể.
     */
    public static final String REPORT = "hopdong-doi-tuong-report";

    private HopDongDoiTuongReportCacheNames() {
    }
}
