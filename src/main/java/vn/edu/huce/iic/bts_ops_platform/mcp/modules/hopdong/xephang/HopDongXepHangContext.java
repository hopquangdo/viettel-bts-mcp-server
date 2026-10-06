package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import java.util.UUID;

/**
 * Bộ lọc đã quy đổi id cho 1 lần xếp hạng hợp đồng. Strategy tự chạy truy vấn của chiều mình (1 câu SQL lá).
 *
 * @param top số dòng tối đa (1..20)
 */
public record HopDongXepHangContext(UUID khuVucId, UUID tinhThanhId, UUID nhaThauId, UUID hopDongId, UUID doiTuongId,
                                    UUID loaiId, UUID kieuId, int top) {
}
