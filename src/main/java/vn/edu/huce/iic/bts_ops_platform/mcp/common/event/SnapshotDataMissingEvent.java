package vn.edu.huce.iic.bts_ops_platform.mcp.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired by core.hopdong.HopDongDoiTuongSnapshotService when it had to serve a snapshot with
 * only the static (core-owned) fields populated — the dynamic fields (sản lượng/vướng mắc)
 * were missing from cache (cold start, TTL expiry, manual flush...). Core does not know or
 * care who answers: it just asks "someone please recompute the dynamic slice for this id".
 *
 * <p>Each owning module (business.sanluong, business.vuongmac) listens for this and responds
 * by computing its own slice and pushing it back via
 * HopDongDoiTuongSnapshotService.applySanLuongFields/applyVuongMacFields — the same patch API
 * their regular SanLuongChangedEvent/VuongMacChangedEvent listeners already use. Core never
 * calls into business to ask for this directly, keeping the dependency one-directional.
 */
public record SnapshotDataMissingEvent(UUID hopDongDoiTuongId, Instant changedAt) {

    public static SnapshotDataMissingEvent of(UUID hopDongDoiTuongId) {
        return new SnapshotDataMissingEvent(hopDongDoiTuongId, Instant.now());
    }
}
