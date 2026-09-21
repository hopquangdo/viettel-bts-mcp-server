package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fired when one or more hợp đồng đối tượng are soft-deleted — i.e. after
 * HopDongDoiTuongService.deleteBatch()/deleteByFilter(). Core has already resolved the exact
 * affected ids (whether the caller passed an explicit list or a filter), so consumers just
 * cascade-soft-delete their own records for these ids without needing to know the original
 * filter criteria.
 */
public record HopDongDoiTuongCascadeDeleteEvent(List<UUID> hopDongDoiTuongIds, Instant changedAt) {

    public static HopDongDoiTuongCascadeDeleteEvent of(List<UUID> hopDongDoiTuongIds) {
        return new HopDongDoiTuongCascadeDeleteEvent(hopDongDoiTuongIds, Instant.now());
    }
}
