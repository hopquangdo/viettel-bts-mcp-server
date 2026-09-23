package vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong;

import java.util.UUID;

/**
 * Tham chiếu id + mã + tên của 1 đối tượng (trạm) — dùng cả để nhúng vào response (thay flatten
 * maDoiTuong/tenDoiTuong) và làm kết quả resolve filter của DoiTuongComponent. Khi nhúng vào
 * response chỉ theo mã (không có id sẵn), dùng {@link #of(String, String)} — id sẽ là null.
 */
public record DoiTuongInfo(UUID id, String ma, String ten) {

    public static DoiTuongInfo of(String ma, String ten) {
        if ((ma == null || ma.isBlank()) && (ten == null || ten.isBlank())) {
            return null;
        }
        return new DoiTuongInfo(null, ma, ten);
    }
}
