package vn.edu.huce.iic.bts_ops_platform.common.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Generic wrapper for bulk id → tên hiển thị lookup maps (nhà thầu, tỉnh/thành, khu vực...).
 * Tồn tại chủ yếu để cache được — {@link vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService}
 * cần một Class cụ thể để deserialize, không cache trực tiếp {@code Map<UUID, String>} (type erasure).
 */
public record NameLookupResponse(Map<UUID, String> items) {

    public NameLookupResponse {
        items = items == null ? Map.of() : Map.copyOf(items);
    }
}
