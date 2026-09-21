package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired whenever a VuongMac record is created/updated/removed for an đối tượng hợp đồng.
 * Consumed to refresh the "soVuongMac" count on the shared HopDongDoiTuongSnapshot
 * cache entry, mirroring how SanLuongChangedEvent refreshes tongThanhTien/hạng mục.
 */
public record VuongMacChangedEvent(
        UUID vuongMacId,
        UUID hopDongId,
        UUID hopDongDoiTuongId,
        Instant changedAt) {

    public static VuongMacChangedEvent of(UUID vuongMacId, UUID hopDongId, UUID hopDongDoiTuongId) {
        return new VuongMacChangedEvent(vuongMacId, hopDongId, hopDongDoiTuongId, Instant.now());
    }
}
