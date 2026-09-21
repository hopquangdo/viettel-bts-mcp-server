package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a hợp đồng's tên/loại hợp đồng changes — i.e. after HopDongService.update().
 * Affects every hợp đồng đối tượng under this hopDongId, so consumers evict by prefix
 * (see HopDongDoiTuongCacheKeys.prefixForHopDong) instead of recomputing eagerly, since
 * the affected object id list isn't cheaply available here.
 */
public record HopDongMetaChangedEvent(UUID hopDongId, Instant changedAt) {

    public static HopDongMetaChangedEvent of(UUID hopDongId) {
        return new HopDongMetaChangedEvent(hopDongId, Instant.now());
    }
}
