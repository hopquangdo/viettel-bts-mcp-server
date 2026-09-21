package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.cache;

public final class NguonLucCacheKeys {

    private static final String SEPARATOR = "|";

    private NguonLucCacheKeys() {
    }

    public static String topHopDong(String normalizedMetric, int top) {
        return String.join(SEPARATOR, NguonLucCacheNames.TOP_HOPDONG_PREFIX, normalizedMetric, String.valueOf(top));
    }
}
