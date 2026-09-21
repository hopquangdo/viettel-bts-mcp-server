package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import java.util.UUID;

/**
 * Tham chiếu id + mã + tên của 1 hợp đồng — dùng cả để nhúng vào response (thay flatten
 * hopDongId/maHopDong/tenHopDong rải rác) và làm kết quả resolve filter của HopDongComponent.
 */
public record HopDongInfo(UUID id, String ma, String ten) {

    public static HopDongInfo of(UUID id, String ma, String ten) {
        if (id == null && (ma == null || ma.isBlank()) && (ten == null || ten.isBlank())) {
            return null;
        }
        return new HopDongInfo(id, ma, ten);
    }
}
