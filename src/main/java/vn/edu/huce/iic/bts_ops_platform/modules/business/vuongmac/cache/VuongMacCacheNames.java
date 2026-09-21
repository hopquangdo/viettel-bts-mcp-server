package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.cache;

public final class VuongMacCacheNames {

    /** VuongMacResponse đã enrich, key = vuongMac.id — xem VuongMacService.getEnriched(). */
    public static final String ROW = "vuongmac-row";

    /** Đếm theo trạng thái (dashboard) — key = chữ ký tập trangThai. */
    public static final String STATS = "vuongmac-stats";

    private VuongMacCacheNames() {
    }
}
