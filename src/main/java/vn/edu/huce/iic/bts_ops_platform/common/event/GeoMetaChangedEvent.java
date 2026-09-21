package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.time.Instant;

/**
 * Fired when a tỉnh/thành or khu vực's tên/mã changes — i.e. after
 * TinhThanhService.create/update or KhuVucService.create/update. Can affect an unbounded
 * number of hợp đồng đối tượng across every hợp đồng, so consumers evict their entire
 * geo-derived cache rather than trying to compute the affected scope here.
 */
public record GeoMetaChangedEvent(Instant changedAt) {

    public static GeoMetaChangedEvent now() {
        return new GeoMetaChangedEvent(Instant.now());
    }
}
