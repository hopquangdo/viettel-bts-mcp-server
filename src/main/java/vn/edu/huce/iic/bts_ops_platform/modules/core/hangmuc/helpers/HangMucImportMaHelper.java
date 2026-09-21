package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HangMucImportMaHelper {

    private HangMucImportMaHelper() {
    }

    public static String buildContractSuffix(HopDongResponse hopDong, UUID hopDongId) {
        String maHopDong = hopDong.getMaHopDong();
        if (HangMucImportTextHelper.isBlank(maHopDong)) {
            return fallbackContractSuffix(hopDongId);
        }
        String value = maHopDong.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return value.isBlank() ? fallbackContractSuffix(hopDongId)
                : (value.length() > 12 ? value.substring(0, 12) : value);
    }

    public static String fallbackContractSuffix(UUID hopDongId) {
        return hopDongId.toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    public static String buildDefaultImportGroupMa(String contractSuffix, UUID hopDongId) {
        String hopDongKey = hopDongId.toString().replace("-", "").toUpperCase(Locale.ROOT);
        if (hopDongKey.length() > 8) {
            hopDongKey = hopDongKey.substring(0, 8);
        }
        return buildContractMa(contractSuffix, HangMucImportFields.MA_PREFIX_HD, hopDongKey);
    }

    public static String buildGroupedNhomMa(String contractSuffix, String tenNhomHangMuc) {
        if (HangMucImportTextHelper.isBlank(tenNhomHangMuc)) {
            return buildContractMa(contractSuffix, HangMucImportFields.MA_PREFIX_HM, HangMucImportFields.DEFAULT_NHOM_MA_SLUG);
        }
        return buildContractMa(contractSuffix, HangMucImportFields.MA_PREFIX_HM, buildHangMucSlug(tenNhomHangMuc));
    }

    public static String buildDefaultChiTietMa(String contractSuffix, String tenNhomHangMuc) {
        return buildContractMa(contractSuffix, buildHangMucSlug(tenNhomHangMuc), HangMucImportFields.MA_PREFIX_DS);
    }

    public static String buildContractMa(String contractSuffix, String... segments) {
        StringBuilder builder = new StringBuilder(contractSuffix);
        for (String segment : segments) {
            builder.append('_').append(segment);
        }
        String ma = builder.toString().toUpperCase(Locale.ROOT);
        return truncateMa(ma);
    }

    public static String buildHangMucSlug(String text) {
        if (HangMucImportTextHelper.isBlank(text)) {
            return HangMucImportFields.DEFAULT_NHOM_MA_SLUG;
        }
        String normalized = TextUtils.stripAccents(text)
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        if (!normalized.isBlank()) {
            return normalized.length() > 16 ? normalized.substring(0, 16) : normalized;
        }
        return Integer.toHexString(text.hashCode()).toUpperCase(Locale.ROOT);
    }

    public static String normalizeMappedMa(String rawMa) {
        return EntityFilter.normalizeCode(rawMa.replaceAll("[^A-Za-z0-9_-]", ""));
    }

    public static String nextAutoMa(String contractSuffix, ImportMaPrefix prefix, int seq) {
        String ma = contractSuffix + "_" + prefix.name() + seq;
        return truncateMa(ma);
    }

    public static int computeNextSeq(Set<String> mas, String prefix) {
        int max = 0;
        String prefixLower = prefix.toLowerCase(Locale.ROOT);
        for (String ma : mas) {
            if (ma == null || !ma.toLowerCase(Locale.ROOT).startsWith(prefixLower)) {
                continue;
            }
            String suffix = ma.toLowerCase(Locale.ROOT).substring(prefixLower.length());
            int end = 0;
            while (end < suffix.length() && Character.isDigit(suffix.charAt(end))) {
                end++;
            }
            if (end > 0) {
                try {
                    max = Math.max(max, Integer.parseInt(suffix.substring(0, end)));
                } catch (NumberFormatException ignored) {
                    // bỏ qua mã không theo pattern
                }
            }
        }
        return max + 1;
    }

    public static String allocateUniqueMa(
            String baseMa,
            Map<String, ?> activeByMa,
            Set<String> occupiedMaLower,
            Set<String> reservedMaLower) {
        String candidate = EntityFilter.normalizeCode(baseMa);
        String key = HangMucImportFields.maKey(candidate);
        if (!reservedMaLower.contains(key)
                && !activeByMa.containsKey(key)
                && !occupiedMaLower.contains(key)) {
            reservedMaLower.add(key);
            occupiedMaLower.add(key);
            return candidate;
        }
        String root = truncateMa(candidate);
        for (int attempt = 1; attempt <= 999; attempt++) {
            candidate = root.length() > 47 ? root.substring(0, 47) + "_" + attempt : root + "_" + attempt;
            candidate = truncateMa(candidate);
            key = HangMucImportFields.maKey(candidate);
            if (!reservedMaLower.contains(key)
                    && !activeByMa.containsKey(key)
                    && !occupiedMaLower.contains(key)) {
                reservedMaLower.add(key);
                occupiedMaLower.add(key);
                return candidate;
            }
        }
        candidate = truncateMa(root + "_" + Instant.now().toEpochMilli());
        key = HangMucImportFields.maKey(candidate);
        reservedMaLower.add(key);
        occupiedMaLower.add(key);
        return candidate;
    }

    public static String truncateMa(String ma) {
        return ma.length() > HangMucImportFields.MAX_MA_LENGTH
                ? ma.substring(0, HangMucImportFields.MAX_MA_LENGTH)
                : ma;
    }
}
