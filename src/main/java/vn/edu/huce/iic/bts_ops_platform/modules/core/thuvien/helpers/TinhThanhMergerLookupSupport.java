package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Lookup sáp nhập tỉnh từ bảng tinh_thanh — ưu tiên nhóm có la_tinh_cu=true. */
public final class TinhThanhMergerLookupSupport {

    /**
     * Mã 3 ký tự legacy trên HĐ/tuyến → mã tỉnh cũ chính thức (đồng bộ FE
     * tinhThanhMergerLookup.ts).
     */
    public static final Map<String, String> LEGACY_MA_TO_OFFICIAL_OLD = Map.ofEntries(
            Map.entry("TBH", "TB"),
            Map.entry("HDG", "HD"),
            Map.entry("BGG", "BG"),
            Map.entry("VPC", "VP"),
            Map.entry("HBH", "HB"),
            Map.entry("BKN", "BK"),
            Map.entry("HGG", "HG"),
            Map.entry("YBI", "YB"),
            Map.entry("QBH", "QB"),
            Map.entry("KTM", "KT"),
            Map.entry("BDH", "BD2"),
            Map.entry("PYN", "PY"),
            Map.entry("NTN", "NTH"),
            Map.entry("BTN", "BTH"),
            Map.entry("DCN", "DKN"),
            Map.entry("BDG", "BD"),
            Map.entry("VTU", "BRV"),
            Map.entry("BPC", "BP"),
            Map.entry("LAN", "LA"),
            Map.entry("TVH", "TV"),
            Map.entry("STG", "ST"),
            Map.entry("HUG", "HGI"));

    private TinhThanhMergerLookupSupport() {
    }

    public record MergerMaps(
            Map<String, String> tinhMoiMaByKey,
            Map<String, String> displayMaByKey) {
    }

    public static MergerMaps buildMergerMaps(List<TinhThanh> allTinh) {
        Map<java.util.UUID, List<TinhThanh>> byGroup = allTinh.stream()
                .collect(Collectors.groupingBy(TinhThanh::getTinhThanhId));

        Map<String, String> tinhMoiMaByKey = new HashMap<>();
        Map<String, String> displayMaByKey = new HashMap<>();

        for (List<TinhThanh> group : byGroup.values()) {
            List<TinhThanh> oldRecords = group.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getLaTinhCu()))
                    .toList();
            if (oldRecords.isEmpty()) {
                continue;
            }
            TinhThanh current = group.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getLaTinhCu()))
                    .findFirst()
                    .orElse(null);
            if (current == null || current.getMa() == null || current.getMa().isBlank()) {
                continue;
            }
            String newMa = current.getMa().trim().toUpperCase(Locale.ROOT);
            for (TinhThanh member : group) {
                registerKey(tinhMoiMaByKey, member.getMa(), newMa);
                registerKey(tinhMoiMaByKey, member.getTen(), newMa);
                String memberMa = member.getMa() == null ? "" : member.getMa().trim().toUpperCase(Locale.ROOT);
                registerKey(displayMaByKey, member.getMa(), memberMa);
                registerKey(displayMaByKey, member.getTen(), memberMa);
            }
        }

        for (Map.Entry<String, String> alias : LEGACY_MA_TO_OFFICIAL_OLD.entrySet()) {
            String tinhMoi = tinhMoiMaByKey.get(alias.getValue().toUpperCase(Locale.ROOT));
            if (tinhMoi == null || tinhMoi.isBlank()) {
                tinhMoi = tinhMoiMaByKey.get(TextUtils.normalizeLower(alias.getValue()));
            }
            if (tinhMoi != null && !tinhMoi.isBlank()) {
                registerKey(tinhMoiMaByKey, alias.getKey(), tinhMoi);
                displayMaByKey.putIfAbsent(alias.getKey().toUpperCase(Locale.ROOT), alias.getKey().toUpperCase(Locale.ROOT));
            }
        }

        for (List<TinhThanh> group : byGroup.values()) {
            for (TinhThanh member : group) {
                if (member.getMa() == null || member.getMa().isBlank()) {
                    continue;
                }
                String memberMa = member.getMa().trim().toUpperCase(Locale.ROOT);
                registerKey(displayMaByKey, member.getMa(), memberMa);
                registerKey(displayMaByKey, member.getTen(), memberMa);
                registerIfAbsent(tinhMoiMaByKey, member.getMa(), memberMa);
                registerIfAbsent(tinhMoiMaByKey, member.getTen(), memberMa);
            }
        }

        return new MergerMaps(tinhMoiMaByKey, displayMaByKey);
    }

    public static String resolveTinhMoiMa(String displayMa, MergerMaps maps) {
        if (displayMa == null || displayMa.isBlank() || maps == null) {
            return displayMa == null ? "" : displayMa.trim().toUpperCase(Locale.ROOT);
        }
        String upper = displayMa.trim().toUpperCase(Locale.ROOT);
        String resolved = maps.tinhMoiMaByKey().get(upper);
        if (resolved == null || resolved.isBlank()) {
            resolved = maps.tinhMoiMaByKey().get(TextUtils.normalizeLower(displayMa));
        }
        return resolved != null && !resolved.isBlank() ? resolved : upper;
    }

    private static void registerKey(Map<String, String> target, String rawKey, String value) {
        if (rawKey == null || rawKey.isBlank() || value == null || value.isBlank()) {
            return;
        }
        target.put(rawKey.trim().toUpperCase(Locale.ROOT), value);
        target.put(TextUtils.normalizeLower(rawKey), value);
    }

    private static void registerIfAbsent(Map<String, String> target, String rawKey, String value) {
        if (rawKey == null || rawKey.isBlank() || value == null || value.isBlank()) {
            return;
        }
        String upper = rawKey.trim().toUpperCase(Locale.ROOT);
        if (!target.containsKey(upper)) {
            registerKey(target, rawKey, value);
        }
    }
}
