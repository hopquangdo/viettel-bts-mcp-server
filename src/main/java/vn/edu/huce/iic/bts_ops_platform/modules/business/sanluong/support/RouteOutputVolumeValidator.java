package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.support;

import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.ContractWorkItem;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.exception.SanLuongErrorCode;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Ràng buộc KM thiết kế lũy kế ≤ KM khảo sát lũy kế (tuyến TVTK) — khớp FE
 * {@code validateRouteOutputVolumes}.
 */
public final class RouteOutputVolumeValidator {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private RouteOutputVolumeValidator() {
    }

    public static void assertValid(List<ContractWorkItem> catalog, Map<UUID, BigDecimal> amountByCatalogKey) {
        if (catalog == null || catalog.isEmpty() || amountByCatalogKey == null) {
            return;
        }
        if (findDesignItem(catalog) == null || findSurveyItem(catalog) == null) {
            return;
        }

        BigDecimal surveyKm = resolveSurveyCumulativeKm(catalog, amountByCatalogKey);
        BigDecimal designKm = resolveDesignCumulativeKm(catalog, amountByCatalogKey);

        if (designKm.signum() <= 0) {
            return;
        }
        if (surveyKm.signum() <= 0) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_ROUTE_DESIGN_EXCEEDS_SURVEY,
                    "Cần có KM khảo sát trước khi bổ sung sản lượng thiết kế");
        }
        if (designKm.compareTo(surveyKm) > 0) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_ROUTE_DESIGN_EXCEEDS_SURVEY,
                    String.format(
                            "KL thiết kế lũy kế (%s) không được vượt KL khảo sát lũy kế (%s)",
                            stripPlain(designKm),
                            stripPlain(surveyKm)));
        }
    }

    private static ContractWorkItem findSurveyItem(List<ContractWorkItem> catalog) {
        if (catalog.isEmpty()) {
            return null;
        }
        if (catalog.size() >= 2) {
            return catalog.get(0);
        }
        return catalog.stream()
                .filter(item -> isSurveyHangMucName(item.name()))
                .findFirst()
                .orElse(catalog.get(0));
    }

    private static ContractWorkItem findDesignItem(List<ContractWorkItem> catalog) {
        if (catalog.size() < 2) {
            return null;
        }
        return catalog.get(1);
    }

    private static BigDecimal resolveSurveyCumulativeKm(
            List<ContractWorkItem> catalog, Map<UUID, BigDecimal> amountByCatalogKey) {
        BigDecimal fromIndex = amountFor(catalog, amountByCatalogKey, 0);
        BigDecimal fromNamed = catalog.stream()
                .filter(item -> isSurveyHangMucName(item.name()))
                .map(item -> amountFor(item, amountByCatalogKey))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return fromIndex.max(fromNamed);
    }

    private static BigDecimal resolveDesignCumulativeKm(
            List<ContractWorkItem> catalog, Map<UUID, BigDecimal> amountByCatalogKey) {
        BigDecimal fromIndex = amountFor(catalog, amountByCatalogKey, 1);
        BigDecimal fromNamed = catalog.stream()
                .filter(item -> isDesignHangMucName(item.name()))
                .map(item -> amountFor(item, amountByCatalogKey))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return fromIndex.max(fromNamed);
    }

    private static BigDecimal amountFor(List<ContractWorkItem> catalog, Map<UUID, BigDecimal> sums, int index) {
        if (index < 0 || index >= catalog.size()) {
            return BigDecimal.ZERO;
        }
        return amountFor(catalog.get(index), sums);
    }

    private static BigDecimal amountFor(ContractWorkItem item, Map<UUID, BigDecimal> sums) {
        if (item == null || item.catalogKey() == null) {
            return BigDecimal.ZERO;
        }
        return sums.getOrDefault(item.catalogKey(), BigDecimal.ZERO).max(BigDecimal.ZERO);
    }

    static boolean isSurveyHangMucName(String name) {
        String normalized = normalizeName(name);
        return normalized.contains("khao sat") || normalized.contains("survey");
    }

    static boolean isDesignHangMucName(String name) {
        String normalized = normalizeName(name);
        return normalized.contains("thiet ke")
                || normalized.contains("design")
                || normalized.contains("thi cong")
                || normalized.contains("construction");
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String nfd = Normalizer.normalize(name.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return DIACRITICS.matcher(nfd).replaceAll("").replace('đ', 'd');
    }

    private static String stripPlain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
