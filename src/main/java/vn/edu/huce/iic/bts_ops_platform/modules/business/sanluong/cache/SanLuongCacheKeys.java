package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache;

import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds cache keys for {@link SanLuongCacheNames#TONG_HOP}. hopDongId always leads the key
 * so a single prefix match (see {@link #tongHopPrefixForHopDong}) can evict every
 * filter/user variant cached for one hợp đồng when a Kafka change event arrives,
 * without touching entries belonging to other contracts.
 */
public final class SanLuongCacheKeys {

    private static final String SEPARATOR = "|";

    private SanLuongCacheKeys() {
    }

    public static String tongHop(
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            String currentUserId,
            String search,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        String doiTuongPart = "";
        if (doiTuongQuanLyIds != null && !doiTuongQuanLyIds.isEmpty()) {
            doiTuongPart = doiTuongQuanLyIds.stream()
                    .sorted()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
        }
        return String.join(SEPARATOR,
                hopDongId != null ? hopDongId.toString() : "",
                doiTuongPart,
                currentUserId != null ? currentUserId : "anonymous",
                EntityFilter.nullToEmpty(search).toLowerCase(Locale.ROOT),
                contractorId != null ? contractorId.toString() : "",
                dateFrom != null ? dateFrom.toString() : "",
                dateTo != null ? dateTo.toString() : "");
    }

    public static String tongHopPrefixForHopDong(UUID hopDongId) {
        return (hopDongId != null ? hopDongId.toString() : "") + SEPARATOR;
    }
}
