package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.KyLoc;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.request.BienBanFilter;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/** Pure normalization for Biên bản status and date filters. */
public final class BienBanCalc {

    private BienBanCalc() {
    }

    public static BienBanFilter normalizeFilter(BienBanFilter filter) {
        Objects.requireNonNull(filter);
        KyLoc ky = filter.ky();
        if (ky == null) {
            return new BienBanFilter(filter.phamVi(), normalizeTrangThai(filter.trangThai()), null);
        }

        LocalDate fromDate = ky.fromDate();
        LocalDate toDate = ky.toDate();
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            LocalDate earlier = toDate;
            toDate = fromDate;
            fromDate = earlier;
        }
        return new BienBanFilter(filter.phamVi(), normalizeTrangThai(filter.trangThai()),
                new KyLoc(fromDate, toDate, ky.chiaTheo()));
    }

    public static String normalizeTrangThai(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return switch (normalized) {
            case "cho_duyet", "choduyet", "pending", "cho_phe_duyet" -> "cho_duyet";
            case "da_duyet", "daduyet", "approved", "da_phe_duyet" -> "da_duyet";
            case "tu_choi", "tuchoi", "rejected", "bi_tu_choi" -> "tu_choi";
            default -> normalized;
        };
    }
}