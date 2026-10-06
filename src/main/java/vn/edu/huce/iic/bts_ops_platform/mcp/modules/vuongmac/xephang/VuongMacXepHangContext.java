package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import java.time.Instant;
import java.util.UUID;

/**
 * Bộ lọc đã quy đổi cho 1 lần xếp hạng vướng mắc đang mở. Strategy tự chạy truy vấn của chiều mình (1 câu SQL lá).
 *
 * @param top số dòng tối đa (1..10)
 */
public record VuongMacXepHangContext(UUID hopDongId, UUID nhaThauId, UUID tinhThanhId, UUID doiTuongId, UUID khuVucId,
                                     String giaiDoanLoc, String kieuLoc, UUID loaiId, UUID canBoId,
                                     Instant ngayTaoFrom, Instant ngayTaoTo, int top) {
}
