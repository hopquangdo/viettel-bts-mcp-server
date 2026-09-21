package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.helpers;

import java.util.List;
import java.util.Locale;

public final class PhanCongGiaiDoanHelper {

    private PhanCongGiaiDoanHelper() {
    }

    /** Khớp mã trạng thái HĐ với bộ lọc giai đoạn trên UI (ks/tk/dt/tc/nt). */
    public static boolean matchesPhaseFilter(String statusMa, String phaseFilter) {
        if (phaseFilter == null || phaseFilter.isBlank() || "all".equalsIgnoreCase(phaseFilter)) {
            return true;
        }
        if (statusMa == null || statusMa.isBlank()) {
            return "unassigned".equalsIgnoreCase(phaseFilter);
        }
        String ma = statusMa.trim().toUpperCase(Locale.ROOT);
        String filter = phaseFilter.trim().toLowerCase(Locale.ROOT);
        return switch (filter) {
            case "ks" -> ma.equals("CKS") || ma.equals("KS") || ma.contains("KHAO SAT") || ma.contains("KHẢO SÁT");
            case "tk" -> ma.equals("TK") || ma.contains("THIET KE") || ma.contains("THIẾT KẾ");
            case "dt" -> ma.equals("DT") || ma.contains("DU TOAN") || ma.contains("DỰ TOÁN");
            case "tc" -> ma.equals("TC") || ma.contains("THI CONG") || ma.contains("THI CÔNG")
                    || ma.equals("DANG_TC") || ma.equals("CHUA_TC");
            case "nt" -> ma.equals("HT") || ma.equals("NT") || ma.equals("XN")
                    || ma.contains("NGHIEM THU") || ma.contains("NGHIỆM THU")
                    || ma.contains("HOAN THANH") || ma.contains("HOÀN THÀNH");
            case "qt" -> ma.equals("QT") || ma.equals("CQT")
                    || ma.contains("QUYET TOAN") || ma.contains("QUYẾT TOÁN");
            default -> ma.equalsIgnoreCase(phaseFilter);
        };
    }

    public static boolean isCompletedStatusMa(String statusMa) {
        if (statusMa == null || statusMa.isBlank()) {
            return false;
        }
        String ma = statusMa.trim().toUpperCase(Locale.ROOT);
        return ma.equals("HT") || ma.equals("QT") || ma.equals("HOAN_THANH")
                || ma.contains("QUYET TOAN") || ma.contains("QUYẾT TOÁN")
                || ma.contains("HOAN THANH") || ma.contains("HOÀN THÀNH");
    }

    private static final List<String> CONTRACTOR_COLORS = List.of(
            "#6366f1", "#E31C23", "#2563eb", "#059669", "#d97706", "#9333ea", "#0891b2");

    public static String colorForKey(String key) {
        if (key == null || key.isBlank()) {
            return CONTRACTOR_COLORS.get(0);
        }
        int index = Math.abs(key.hashCode()) % CONTRACTOR_COLORS.size();
        return CONTRACTOR_COLORS.get(index);
    }
}
