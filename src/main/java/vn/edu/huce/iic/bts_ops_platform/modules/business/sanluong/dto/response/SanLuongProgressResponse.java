package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import java.math.BigDecimal;

/**
 * Tiến độ hạng mục + tổng thành tiền của 1 đối tượng hợp đồng — dùng làm nguồn
 * cho cache mô tả dùng chung giữa module volume và sanluong (xem
 * HopDongDoiTuongSnapshotService).
 */
public record SanLuongProgressResponse(
        int itemsDone,
        int itemsTotal,
        int issueCount,
        BigDecimal tongThanhTien) {
}
