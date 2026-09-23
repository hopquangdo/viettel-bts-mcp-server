package vn.edu.huce.iic.bts_ops_platform.mcp.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a single hợp đồng đối tượng's descriptive attributes (mã đối tượng,
 * tỉnh, khu vực, nhà thầu) change — i.e. after HopDongDoiTuongService.update().
 * Consumed to refresh the shared HopDongDoiTuongSnapshot cache entry.
 */
public record HopDongDoiTuongMetaChangedEvent(
        UUID hopDongId,
        UUID hopDongDoiTuongId,
        Instant changedAt) {

    public static HopDongDoiTuongMetaChangedEvent of(UUID hopDongId, UUID hopDongDoiTuongId) {
        return new HopDongDoiTuongMetaChangedEvent(hopDongId, hopDongDoiTuongId, Instant.now());
    }
}
