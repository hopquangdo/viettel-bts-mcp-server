package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a hợp đồng is canceled (hủy) — i.e. after HopDongService.cancel(). Unlike
 * HopDongDeletedEvent (soft-delete, sets ngayXoa), cancel only sets hoatDong=false. Consumers
 * mirror that: deactivate their own records for this hopDongId without touching ngayXoa.
 */
public record HopDongCanceledEvent(UUID hopDongId, Instant changedAt) {

    public static HopDongCanceledEvent of(UUID hopDongId) {
        return new HopDongCanceledEvent(hopDongId, Instant.now());
    }
}
