package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired whenever a SanLuong record is created/updated/removed. Carries just enough
 * to identify the affected hợp đồng/đối tượng so consumers (kể cả module khác —
 * ví dụ hopdong denormalize ngày thi công gần nhất) có thể phản ứng mà không phải
 * tiêm thẳng vào nội bộ module sanluong.
 */
public record SanLuongChangedEvent(
        UUID hopDongId,
        UUID hopDongDoiTuongId,
        Instant changedAt) {

    public static SanLuongChangedEvent of(UUID hopDongId, UUID hopDongDoiTuongId) {
        return new SanLuongChangedEvent(hopDongId, hopDongDoiTuongId, Instant.now());
    }
}
