package vn.edu.huce.iic.bts_ops_platform.mcp.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired after a hợp đồng is soft-deleted. Shared payload so hopdong (producer) and
 * every downstream module (sanluong, hangmuc, vuongmac, phancong) that owns
 * records referencing hopDongId can depend on this record instead of on each
 * other's repositories/services.
 */
public record HopDongDeletedEvent(UUID hopDongId, Instant deletedAt) {

    public static HopDongDeletedEvent of(UUID hopDongId) {
        return new HopDongDeletedEvent(hopDongId, Instant.now());
    }
}
