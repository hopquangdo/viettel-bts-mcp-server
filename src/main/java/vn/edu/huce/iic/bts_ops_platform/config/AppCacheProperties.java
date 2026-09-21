package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.cache")
public record AppCacheProperties(
        boolean enabled,
        String keyPrefix,
        long contractorScopeTtlSeconds,
        long phanquyenAuthorityTtlSeconds,
        long sanluongTongHopTtlSeconds,
        long sanluongDoiTuongRowTtlSeconds,
        long nameLookupTtlSeconds,
        long volumeBatchTtlSeconds,
        long hopDongDoiTuongSnapshotTtlSeconds,
        long vuongMacRowTtlSeconds,
        long hopDongDoiTuongReportTtlSeconds) {

    public Duration contractorScopeTtl() {
        return Duration.ofSeconds(contractorScopeTtlSeconds);
    }

    public Duration phanquyenAuthorityTtl() {
        return Duration.ofSeconds(phanquyenAuthorityTtlSeconds);
    }

    public Duration sanluongTongHopTtl() {
        return Duration.ofSeconds(sanluongTongHopTtlSeconds);
    }

    public Duration sanluongDoiTuongRowTtl() {
        return Duration.ofSeconds(sanluongDoiTuongRowTtlSeconds);
    }

    /** Dùng cho các bulk id→tên lookup ít đổi (nhà thầu, tỉnh/thành, khu vực...). */
    public Duration nameLookupTtl() {
        return Duration.ofSeconds(nameLookupTtlSeconds);
    }

    /** Dùng cho VolumeServiceImpl.loadBatch() — phụ thuộc sản lượng nên TTL ngắn. */
    public Duration volumeBatchTtl() {
        return Duration.ofSeconds(volumeBatchTtlSeconds);
    }

    /**
     * Dùng cho HopDongDoiTuongSnapshotService.getSnapshots() — TTL dài vì cache được chủ động
     * refresh/evict qua event (SanLuongChangedEvent, VuongMacChangedEvent,
     * HopDongDoiTuongMetaChangedEvent, HopDongMetaChangedEvent) thay vì phụ thuộc TTL hết hạn.
     */
    public Duration hopDongDoiTuongSnapshotTtl() {
        return Duration.ofSeconds(hopDongDoiTuongSnapshotTtlSeconds);
    }

    /**
     * Dùng cho VuongMacService (row cache theo id + dashboard stats) — TTL dài, cache
     * được refresh/evict chủ động qua VuongMacChangedEvent/HopDongDoiTuongMetaChangedEvent/
     * HopDongMetaChangedEvent.
     */
    public Duration vuongMacRowTtl() {
        return Duration.ofSeconds(vuongMacRowTtlSeconds);
    }

    /**
     * Dùng cho các report/thống kê đọc nhiều trong HopDongDoiTuongServiceImpl (breakdown khu
     * vực, thiếu cập nhật, sản lượng bất thường, tốc độ hoàn thành) — TTL dài, evict chủ động
     * qua event, xem HopDongAggregateCacheInvalidationListener.
     */
    public Duration hopDongDoiTuongReportTtl() {
        return Duration.ofSeconds(hopDongDoiTuongReportTtlSeconds);
    }
}
