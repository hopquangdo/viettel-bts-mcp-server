package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTienDoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HopDongDanhSachTienDoHelper {

    private HopDongDanhSachTienDoHelper() {
    }

    public static List<HopDongDanhSachTienDoBuocResponse> buildTienDoBuocList(
            List<LuongTrangThaiBuocResponse> flowBuoc,
            Map<String, Long> statusCounts) {
        if (flowBuoc == null || flowBuoc.isEmpty()) {
            return List.of();
        }
        List<HopDongDanhSachTienDoBuocResponse> result = new ArrayList<>();
        for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
            HopDongDanhSachTienDoBuocResponse step = new HopDongDanhSachTienDoBuocResponse();
            step.setTrangThaiHopDongId(buoc.getTrangThaiHopDongId());
            step.setMa(buoc.getMa());
            step.setTen(buoc.getTen());
            step.setMauSac(buoc.getMauSac());
            step.setThuTu(buoc.getThuTu());
            String statusKey = buoc.getMa() != null
                    ? buoc.getMa().trim().toUpperCase(Locale.ROOT)
                    : null;
            step.setSoLuong(statusKey == null ? 0L : statusCounts.getOrDefault(statusKey, 0L));
            result.add(step);
        }
        return result;
    }

    /**
     * Giống {@link #buildTienDoBuocList} nhưng bổ sung các trạng thái có số lượng &gt; 0
     * mà không nằm trong luồng cấu hình (VD: HUY, VM) — dùng cho hiển thị UI.
     */
    public static List<HopDongDanhSachTienDoBuocResponse> buildTienDoBuocListForDisplay(
            List<LuongTrangThaiBuocResponse> flowBuoc,
            Map<String, Long> statusCounts) {
        Map<String, Long> normalized = normalizeStatusCountsForFlow(statusCounts, flowBuoc);
        List<HopDongDanhSachTienDoBuocResponse> result = new ArrayList<>(
                buildTienDoBuocList(flowBuoc, normalized));
        appendOrphanStatusSteps(result, flowBuoc, normalized);
        return result;
    }

    /**
     * Gom các mã trạng thái thực tế (HỦY, …) về mã bước trên luồng cấu hình (HUY, …)
     * để tránh đếm trùng: 1 cột luồng = 0 và 1 cột orphan = N.
     */
    public static Map<String, Long> normalizeStatusCountsForFlow(
            Map<String, Long> statusCounts,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (statusCounts == null || statusCounts.isEmpty()) {
            return statusCounts == null ? Map.of() : statusCounts;
        }
        Map<String, Long> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            long count = entry.getValue() != null ? entry.getValue() : 0L;
            if (count <= 0) {
                continue;
            }
            String key = resolveStatusCountKey(null, entry.getKey(), null, flowBuoc);
            merged.merge(key, count, Long::sum);
        }
        return merged;
    }

    private static void appendOrphanStatusSteps(
            List<HopDongDanhSachTienDoBuocResponse> result,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            Map<String, Long> statusCounts) {
        if (statusCounts == null || statusCounts.isEmpty()) {
            return;
        }
        Set<String> flowKeys = new HashSet<>();
        if (flowBuoc != null) {
            for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
                if (buoc.getMa() != null && !buoc.getMa().isBlank()) {
                    flowKeys.add(buoc.getMa().trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        boolean flowHasHuy = flowBuoc != null && flowBuoc.stream()
                .anyMatch(buoc -> isHuyStatus(buoc.getMa(), buoc.getTen()));
        boolean flowHasVuongMac = flowBuoc != null && flowBuoc.stream()
                .anyMatch(buoc -> isVuongMacStatus(buoc.getMa(), buoc.getTen()));
        Map<String, Long> orphanCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            long count = entry.getValue() != null ? entry.getValue() : 0L;
            if (count <= 0) {
                continue;
            }
            String key = normalizeStatusMa(entry.getKey());
            if (flowKeys.contains(key) || !isDisplayOrphanStatus(key)) {
                continue;
            }
            orphanCounts.merge(orphanBucketKey(key), count, Long::sum);
        }
        for (Map.Entry<String, Long> entry : orphanCounts.entrySet()) {
            if ("HUY".equals(entry.getKey()) && flowHasHuy) {
                mergeOrphanCountIntoFlowStep(result, flowBuoc, entry.getValue(), true);
                continue;
            }
            if ("VM".equals(entry.getKey()) && flowHasVuongMac) {
                mergeOrphanCountIntoFlowStep(result, flowBuoc, entry.getValue(), false);
                continue;
            }
            HopDongDanhSachTienDoBuocResponse step = new HopDongDanhSachTienDoBuocResponse();
            step.setMa(entry.getKey());
            step.setTen(orphanStatusLabel(entry.getKey()));
            step.setMauSac(orphanStatusColor(entry.getKey()));
            step.setSoLuong(entry.getValue());
            result.add(step);
        }
    }

    private static void mergeOrphanCountIntoFlowStep(
            List<HopDongDanhSachTienDoBuocResponse> result,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            long count,
            boolean huy) {
        if (count <= 0 || flowBuoc == null) {
            return;
        }
        for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
            boolean match = huy
                    ? isHuyStatus(buoc.getMa(), buoc.getTen())
                    : isVuongMacStatus(buoc.getMa(), buoc.getTen());
            if (!match) {
                continue;
            }
            UUID targetId = buoc.getTrangThaiHopDongId();
            String targetMa = buoc.getMa() != null ? buoc.getMa().trim().toUpperCase(Locale.ROOT) : null;
            for (HopDongDanhSachTienDoBuocResponse step : result) {
                boolean sameStep = targetId != null && targetId.equals(step.getTrangThaiHopDongId());
                if (!sameStep && targetMa != null) {
                    sameStep = targetMa.equals(normalizeStatusMa(step.getMa()));
                }
                if (sameStep) {
                    step.setSoLuong(step.getSoLuong() + count);
                    return;
                }
            }
            return;
        }
    }

    private static boolean isDisplayOrphanStatus(String key) {
        return isHuyStatus(key, null)
                || key.equals("VM")
                || key.contains("VUONG")
                || key.equals(MA_CHO_XAC_NHAN_HUY)
                || key.equals(MA_CHO_XAC_NHAN_HT);
    }

    private static String orphanBucketKey(String key) {
        if (isHuyStatus(key, null)) {
            return "HUY";
        }
        if (key.equals("VM") || key.contains("VUONG")) {
            return "VM";
        }
        return key;
    }

    private static String orphanStatusLabel(String bucket) {
        return switch (bucket) {
            case "HUY" -> "Hủy";
            case "VM" -> "Vướng mắc";
            case MA_CHO_XAC_NHAN_HUY -> "Chờ xác nhận hủy";
            case MA_CHO_XAC_NHAN_HT -> "Chờ xác nhận hoàn thành";
            default -> bucket;
        };
    }

    private static String orphanStatusColor(String bucket) {
        return switch (bucket) {
            case "HUY" -> "#9333ea";
            case "VM" -> "#dc2626";
            case MA_CHO_XAC_NHAN_HUY, MA_CHO_XAC_NHAN_HT -> "#f59e0b";
            default -> "#64748b";
        };
    }

    public static double computeTyLeHoanThanh(Map<String, Long> statusCounts) {
        if (statusCounts == null || statusCounts.isEmpty()) {
            return 0D;
        }
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        if (total <= 0) {
            return 0D;
        }
        long completed = statusCounts.entrySet().stream()
                .filter(entry -> isCompletedStatus(entry.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();
        return Math.round((completed * 10000D) / total) / 100D;
    }

    /** Gán trạm chưa có mã trạng thái vào bước đầu luồng (thường là CKS). */
    public static Map<String, Long> reconcileUnassignedStatuses(
            Map<String, Long> statusCounts,
            long soDoiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (statusCounts == null || flowBuoc == null || flowBuoc.isEmpty() || soDoiTuong <= 0) {
            return statusCounts;
        }
        long mapped = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long unassigned = soDoiTuong - mapped;
        if (unassigned <= 0) {
            return statusCounts;
        }
        Map<String, Long> merged = new LinkedHashMap<>(statusCounts);
        LuongTrangThaiBuocResponse first = flowBuoc.stream()
                .min(Comparator.comparing(
                        LuongTrangThaiBuocResponse::getThuTu,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(flowBuoc.get(0));
        String key = first.getMa() != null
                ? first.getMa().trim().toUpperCase(Locale.ROOT)
                : "UNKNOWN";
        merged.merge(key, unassigned, Long::sum);
        return merged;
    }

    /**
     * Tiến độ theo vị trí bước trong luồng: bước thứ i đóng góp (i + 1) / số bước;
     * Hủy loại khỏi mẫu số.
     */
    public static double computeTyLeHoanThanhByWorkflow(
            Map<String, Long> statusCounts,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            long soDoiTuong) {
        if (flowBuoc == null || flowBuoc.isEmpty() || soDoiTuong <= 0 || statusCounts == null) {
            return 0D;
        }
        List<LuongTrangThaiBuocResponse> ordered = flowBuoc.stream()
                .sorted(Comparator.comparing(
                        LuongTrangThaiBuocResponse::getThuTu,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<LuongTrangThaiBuocResponse> ladder = ordered.stream()
                .filter(step -> !isHuyStatus(step.getMa(), step.getTen()))
                .toList();
        if (ladder.isEmpty()) {
            return 0D;
        }
        Map<String, Integer> indexByMa = new LinkedHashMap<>();
        for (int i = 0; i < ladder.size(); i++) {
            LuongTrangThaiBuocResponse step = ladder.get(i);
            if (step.getMa() != null && !step.getMa().isBlank()) {
                indexByMa.put(normalizeStatusMa(step.getMa()), i);
            }
        }
        long cancelled = 0L;
        double weighted = 0D;
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            long qty = entry.getValue() != null ? entry.getValue() : 0L;
            if (qty <= 0) {
                continue;
            }
            String key = entry.getKey();
            if (isHuyStatus(key, null)) {
                cancelled += qty;
                continue;
            }
            Integer idx = indexByMa.get(normalizeStatusMa(key));
            if (idx != null) {
                weighted += qty * ((idx + 1D) / ladder.size());
            } else if (isCompletedStatus(key) || isPendingCompleteStatus(key, null)) {
                weighted += qty;
            }
        }
        long denominator = soDoiTuong - cancelled;
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((weighted / denominator) * 10000D) / 100D;
    }

    /** Tỷ lệ hiển thị danh sách HĐ / tỉnh / khu vực — luôn theo luồng khi đã cấu hình. */
    public static double computeDisplayTyLeHoanThanh(
            Map<String, Long> statusCounts,
            long soDoiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (soDoiTuong <= 0) {
            return 0D;
        }
        Map<String, Long> reconciled = reconcileUnassignedStatuses(
                statusCounts != null ? statusCounts : Map.of(), soDoiTuong, flowBuoc);
        if (flowBuoc != null && !flowBuoc.isEmpty()) {
            return computeTyLeHoanThanhByWorkflow(reconciled, flowBuoc, soDoiTuong);
        }
        return computeTyLeHoanThanh(reconciled);
    }

    public static boolean isCompletedStatus(String statusMa) {
        if (statusMa == null) {
            return false;
        }
        String normalized = statusMa.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("HT")
                || normalized.equals("QT")
                || normalized.equals("HOAN_THANH")
                || normalized.contains("QUYET TOAN")
                || normalized.contains("QUYẾT TOÁN")
                || normalized.contains("HOAN THANH")
                || normalized.contains("HOÀN THÀNH");
    }

    /** Tỷ lệ HĐ hoàn thành theo cột trang_thai_thi_cong trên hop_dong. */
    public static double computeTyLeHoanThanhHopDong(Map<String, Long> statusCounts) {
        if (statusCounts == null || statusCounts.isEmpty()) {
            return 0D;
        }
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        if (total <= 0) {
            return 0D;
        }
        long completed = statusCounts.getOrDefault("HOAN_THANH", 0L);
        return Math.round((completed * 10000D) / total) / 100D;
    }

    /** Tiến độ TB theo trạng thái đối tượng — dùng cho thống kê tổng/loại/kiểu. */
    public static double computeAggregateTyLeHoanThanh(
            Map<String, Long> objectStatusCounts,
            long tongDoiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (tongDoiTuong <= 0 || objectStatusCounts == null || objectStatusCounts.isEmpty()) {
            return 0D;
        }
        return computeDisplayTyLeHoanThanh(objectStatusCounts, tongDoiTuong, flowBuoc);
    }

    public static String normalizeStatusMa(String statusMa) {
        if (statusMa == null || statusMa.isBlank()) {
            return "UNKNOWN";
        }
        return statusMa.trim().toUpperCase(Locale.ROOT);
    }

    /** Khớp FE {@code isHuyStep} — ma HUY/HỦY/HUỶ hoặc tên chứa "hủy" (không gồm chờ xác nhận). */
    public static boolean isHuyStatus(String statusMa, String statusTen) {
        if (isPendingCancelStatus(statusMa, statusTen)) {
            return false;
        }
        if (statusMa != null && !statusMa.isBlank()) {
            String normalized = normalizeStatusMa(statusMa);
            if (normalized.equals("HUY")
                    || normalized.contains("HUY")
                    || normalized.contains("HUỶ")
                    || normalized.contains("HỦY")) {
                return true;
            }
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if (ten.contains("hủy") || ten.contains("huy")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVuongMacStatus(String statusMa, String statusTen) {
        if (statusMa != null && !statusMa.isBlank()) {
            String normalized = normalizeStatusMa(statusMa);
            if (normalized.equals("VM") || normalized.contains("VUONG")) {
                return true;
            }
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if (ten.contains("vướng") || ten.contains("vuong")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gom đếm theo mã bước luồng cấu hình (ưu tiên {@code trangThaiHopDongId}),
     * tránh lệch ma thực tế (VD HỦY) với ma trên luồng (VD HUY).
     */
    public static String resolveStatusCountKey(
            UUID trangThaiHopDongId,
            String statusMa,
            String statusTen,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (flowBuoc != null && trangThaiHopDongId != null) {
            for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
                if (trangThaiHopDongId.equals(buoc.getTrangThaiHopDongId())
                        && buoc.getMa() != null
                        && !buoc.getMa().isBlank()) {
                    return normalizeStatusMa(buoc.getMa());
                }
            }
        }
        if (isPendingCancelStatus(statusMa, statusTen)) {
            return MA_CHO_XAC_NHAN_HUY;
        }
        if (isPendingCompleteStatus(statusMa, statusTen)) {
            return MA_CHO_XAC_NHAN_HT;
        }
        if (isHuyStatus(statusMa, statusTen)) {
            if (flowBuoc != null) {
                for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
                    if (isHuyStatus(buoc.getMa(), buoc.getTen())
                            && buoc.getMa() != null
                            && !buoc.getMa().isBlank()) {
                        return normalizeStatusMa(buoc.getMa());
                    }
                }
            }
            return "HUY";
        }
        if (isVuongMacStatus(statusMa, statusTen)) {
            if (flowBuoc != null) {
                for (LuongTrangThaiBuocResponse buoc : flowBuoc) {
                    if (isVuongMacStatus(buoc.getMa(), buoc.getTen())
                            && buoc.getMa() != null
                            && !buoc.getMa().isBlank()) {
                        return normalizeStatusMa(buoc.getMa());
                    }
                }
            }
            return "VM";
        }
        return normalizeStatusMa(statusMa);
    }

    public static final String MA_CHO_XAC_NHAN_HUY = "CXN_HUY";
    public static final String MA_CHO_XAC_NHAN_HT = "CXN_HT";

    public static boolean isPendingCancelStatus(String statusMa, String statusTen) {
        if (statusMa != null && !statusMa.isBlank()) {
            String normalized = normalizeStatusMa(statusMa);
            if (normalized.equals(MA_CHO_XAC_NHAN_HUY)
                    || (normalized.contains("CXN") && normalized.contains("HUY"))) {
                return true;
            }
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if ((ten.contains("chờ") || ten.contains("cho")) && (ten.contains("hủy") || ten.contains("huy"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPendingCompleteStatus(String statusMa, String statusTen) {
        if (statusMa != null && !statusMa.isBlank()) {
            String normalized = normalizeStatusMa(statusMa);
            if (normalized.equals(MA_CHO_XAC_NHAN_HT)
                    || (normalized.contains("CXN") && normalized.contains("HT"))) {
                return true;
            }
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if ((ten.contains("chờ") || ten.contains("cho"))
                    && (ten.contains("hoàn") || ten.contains("hoan") || ten.contains("xn ht"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tư vấn thiết kế — đối tượng còn trong nhóm Chưa làm / Đang làm (chưa HT/Hủy xác nhận).
     * CXN_HUY / CXN_HT vẫn tính theo sản lượng.
     */
    /**
     * Khóa đổi trạng thái thủ công (sửa/import/hàng loạt) — chỉ luồng duyệt được phép.
     */
    public static boolean isTrangThaiThayDoiThuCongBiKhoa(
            LocalDate ngayHtTc,
            Boolean hoatDong,
            String statusMa,
            String statusTen) {
        if (!Boolean.TRUE.equals(hoatDong)) {
            return true;
        }
        if (ngayHtTc != null) {
            return true;
        }
        if (isPendingCancelStatus(statusMa, statusTen)) {
            return true;
        }
        if (isPendingCompleteStatus(statusMa, statusTen)) {
            return true;
        }
        if (isHuyStatus(statusMa, statusTen)) {
            return true;
        }
        if (isCompletedStatus(statusMa)) {
            return true;
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if ((ten.contains("hoàn thành") || ten.contains("hoan thanh"))
                    && !ten.contains("chờ")
                    && !ten.contains("cho")
                    && !ten.contains("quyết")
                    && !ten.contains("quyet")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Khóa xóa đối tượng đã vào nghiệm thu / quyết toán — tránh mất sản lượng & vướng mắc (cascade).
     * Đối tượng chờ duyệt (hoatDong=false) vẫn cho xóa (từ chối import).
     */
    public static boolean isDoiTuongXoaBiKhoa(
            Boolean hoatDong,
            LocalDate ngayHtTc,
            String statusMa,
            String statusTen,
            BigDecimal quyetToanThuc,
            BigDecimal sanLuongHieuLuc) {
        return resolveDoiTuongXoaBiKhoaLyDo(
                hoatDong, ngayHtTc, statusMa, statusTen, quyetToanThuc, sanLuongHieuLuc)
                != null;
    }

    public static String resolveDoiTuongXoaBiKhoaLyDo(
            Boolean hoatDong,
            LocalDate ngayHtTc,
            String statusMa,
            String statusTen,
            BigDecimal quyetToanThuc,
            BigDecimal sanLuongHieuLuc) {
        if (!Boolean.TRUE.equals(hoatDong)) {
            return null;
        }
        BigDecimal quyetToan = VolumeTinhToanHelper.nz(quyetToanThuc);
        if (quyetToan.signum() > 0) {
            return "đã quyết toán";
        }
        if (statusMa != null && !statusMa.isBlank()) {
            String ma = normalizeStatusMa(statusMa);
            if ("QT".equals(ma) || ma.contains("QUYET")) {
                return "đang ở trạng thái quyết toán";
            }
        }
        BigDecimal sanLuong = VolumeTinhToanHelper.nz(sanLuongHieuLuc);
        if (sanLuong.signum() > 0) {
            return "đã có sản lượng thi công nghiệm thu";
        }
        if (ngayHtTc != null) {
            return "đã hoàn thành thi công";
        }
        if (isPendingCompleteStatus(statusMa, statusTen)) {
            return "đang chờ xác nhận hoàn thành";
        }
        if (isPendingCancelStatus(statusMa, statusTen)) {
            return "đang chờ xác nhận hủy";
        }
        if (isCompletedStatus(statusMa)) {
            return "đã hoàn thành thi công";
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if ((ten.contains("hoàn thành") || ten.contains("hoan thanh"))
                    && !ten.contains("chờ")
                    && !ten.contains("cho")
                    && !ten.contains("quyết")
                    && !ten.contains("quyet")) {
                return "đã hoàn thành thi công";
            }
        }
        return null;
    }

    /** Trạng thái tiến độ quyết toán — khóa mọi thao tác đợt quyết toán trên Volume. */
    public static boolean isQuyetToanVolumeDotFullLock(String statusMa, String statusTen) {
        if (statusMa != null && !statusMa.isBlank()) {
            String ma = normalizeStatusMa(statusMa);
            if ("QT".equals(ma) || ma.contains("QUYET")) {
                return true;
            }
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if (ten.contains("quyết toán") || ten.contains("quyet toan")) {
                return true;
            }
        }
        return false;
    }

    /** Khóa sửa/xóa đợt quyết toán — vẫn cho thêm đợt nếu chưa full lock. */
    public static boolean isQuyetToanVolumeDotEditLock(
            BigDecimal quyetToanThuc, String statusMa, String statusTen) {
        if (isQuyetToanVolumeDotFullLock(statusMa, statusTen)) {
            return true;
        }
        return quyetToanThuc != null && quyetToanThuc.signum() > 0;
    }

    public static String resolveQuyetToanVolumeDotLockMessage(
            BigDecimal quyetToanThuc, String statusMa, String statusTen, boolean forCreate) {
        if (isQuyetToanVolumeDotFullLock(statusMa, statusTen)) {
            return "Đối tượng đã ở trạng thái quyết toán — không thể thay đổi các đợt quyết toán";
        }
        if (!forCreate && isQuyetToanVolumeDotEditLock(quyetToanThuc, statusMa, statusTen)) {
            return "Đã ghi nhận quyết toán thực — không thể sửa hoặc xóa đợt quyết toán";
        }
        return null;
    }

    public static boolean countsTowardTuVanSanLuongProgress(
            UUID trangThaiHopDongId,
            String statusMa,
            String statusTen,
            java.time.LocalDate ngayHtTc,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (isHuyStatus(statusMa, statusTen)) {
            return false;
        }
        if (ngayHtTc != null) {
            return false;
        }
        String key = resolveStatusCountKey(trangThaiHopDongId, statusMa, statusTen, flowBuoc);
        if (key.equals("HT") || key.equals("HOAN_THANH")) {
            return false;
        }
        return true;
    }

    /** Bước tiến độ đầu tiên trên luồng (bỏ Hủy/vướng/chờ xác nhận). */
    public static LuongTrangThaiBuocResponse resolveFirstProgressFlowStep(
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (flowBuoc == null || flowBuoc.isEmpty()) {
            return null;
        }
        return flowBuoc.stream()
                .sorted(Comparator.comparing(
                        LuongTrangThaiBuocResponse::getThuTu,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .filter(step -> !isNonProgressFlowStep(step))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tư vấn thiết kế — đối tượng còn ở bước đầu luồng (hoặc chưa gán trạng thái) và chưa có sản lượng.
     */
    public static boolean isTuVanChuaLamStep(String statusKey, List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (statusKey == null || statusKey.isBlank()) {
            return true;
        }
        String normalized = normalizeStatusMa(statusKey);
        if (normalized.equals("UNKNOWN")) {
            return true;
        }
        if (normalized.equals("CKS")
                || normalized.contains("CHUA")
                || normalized.contains("CHƯA")) {
            return true;
        }
        LuongTrangThaiBuocResponse first = resolveFirstProgressFlowStep(flowBuoc);
        if (first == null || first.getMa() == null || first.getMa().isBlank()) {
            return false;
        }
        return normalized.equals(normalizeStatusMa(first.getMa()));
    }

    /**
     * Tìm bước hoàn thành thi công trong luồng (ưu tiên HT/HOAN_THANH, bỏ quyết toán) —
     * dùng khi xác nhận hoàn thành sản lượng dù chưa làm hết hạng mục.
     * Luồng không có HT (VD tư vấn KS→TK→DT): lấy bước tiến độ cuối cùng.
     */
    public static UUID resolveHoanThanhTrangThaiId(List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (flowBuoc == null || flowBuoc.isEmpty()) {
            return null;
        }
        List<LuongTrangThaiBuocResponse> ordered = flowBuoc.stream()
                .sorted(Comparator.comparing(
                        LuongTrangThaiBuocResponse::getThuTu,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        for (LuongTrangThaiBuocResponse step : ordered) {
            String ma = normalizeStatusMa(step.getMa());
            if ("HT".equals(ma) || "HOAN_THANH".equals(ma)) {
                return step.getTrangThaiHopDongId();
            }
        }
        LuongTrangThaiBuocResponse completedFallback = null;
        for (LuongTrangThaiBuocResponse step : ordered) {
            if (step.getMa() != null
                    && isCompletedStatus(step.getMa())
                    && !normalizeStatusMa(step.getMa()).equals("QT")) {
                completedFallback = step;
            }
        }
        if (completedFallback != null) {
            return completedFallback.getTrangThaiHopDongId();
        }
        for (LuongTrangThaiBuocResponse step : ordered) {
            String ma = normalizeStatusMa(step.getMa());
            if ("QT".equals(ma) || ma.contains("QUYET")) {
                continue;
            }
            String ten = step.getTen() != null ? step.getTen().trim().toLowerCase(Locale.ROOT) : "";
            if ((ten.contains("hoàn thành") || ten.contains("hoan thanh"))
                    && !ten.contains("quyết")
                    && !ten.contains("quyet")) {
                return step.getTrangThaiHopDongId();
            }
        }
        LuongTrangThaiBuocResponse lastProgress = null;
        for (LuongTrangThaiBuocResponse step : ordered) {
            if (isNonProgressFlowStep(step)) {
                continue;
            }
            lastProgress = step;
        }
        return lastProgress != null ? lastProgress.getTrangThaiHopDongId() : null;
    }

    /** Mã trạng thái hoàn thành trên luồng — khớp {@link #resolveHoanThanhTrangThaiId}. */
    public static String resolveHoanThanhTrangThaiMa(List<LuongTrangThaiBuocResponse> flowBuoc) {
        UUID id = resolveHoanThanhTrangThaiId(flowBuoc);
        if (id != null && flowBuoc != null) {
            for (LuongTrangThaiBuocResponse step : flowBuoc) {
                if (id.equals(step.getTrangThaiHopDongId())
                        && step.getMa() != null
                        && !step.getMa().isBlank()) {
                    return normalizeStatusMa(step.getMa());
                }
            }
        }
        return "HT";
    }

    public static boolean isObjectHoanThanh(
            LocalDate ngayHtTc,
            String statusMa,
            String statusTen,
            String statusKey) {
        if (ngayHtTc != null) {
            return true;
        }
        if (HopDongDanhSachTienDoHelper.isHuyStatus(statusMa, statusTen)) {
            return false;
        }
        if (isCompletedStatus(statusKey != null ? statusKey : statusMa)) {
            return true;
        }
        if (statusTen != null && !statusTen.isBlank()) {
            String ten = statusTen.trim().toLowerCase(Locale.ROOT);
            if ((ten.contains("hoàn thành") || ten.contains("hoan thanh"))
                    && !ten.contains("chờ")
                    && !ten.contains("cho")
                    && !ten.contains("quyết")
                    && !ten.contains("quyet")) {
                return true;
            }
        }
        return false;
    }

    /** % tiến độ 1 đối tượng: hoàn thành → 100%; có sản lượng → theo SL/kế hoạch; còn lại theo vị trí luồng. */
    public static double resolveDoiTuongTyLeHoanThanh(
            LocalDate ngayHtTc,
            String statusMa,
            String statusTen,
            String statusKey,
            BigDecimal effectiveSanLuong,
            BigDecimal planValue,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (isPendingCompleteStatus(statusMa, statusTen)) {
            return 100D;
        }
        if (isObjectHoanThanh(ngayHtTc, statusMa, statusTen, statusKey)) {
            return 100D;
        }
        if (flowBuoc != null && statusKey != null && !statusKey.isBlank()) {
            String hoanMa = resolveHoanThanhTrangThaiMa(flowBuoc);
            if (hoanMa.equals(normalizeStatusMa(statusKey))) {
                return 100D;
            }
        }
        BigDecimal sanLuong = VolumeTinhToanHelper.nz(effectiveSanLuong);
        BigDecimal keHoach = VolumeTinhToanHelper.nz(planValue);
        if (keHoach.compareTo(BigDecimal.ZERO) > 0) {
            double volumePct = VolumeTinhToanHelper.ratio(sanLuong, keHoach).doubleValue();
            if (volumePct > 0D) {
                return volumePct;
            }
        }
        if (flowBuoc != null && !flowBuoc.isEmpty() && statusKey != null && !statusKey.isBlank()) {
            Map<String, Long> one = Map.of(normalizeStatusMa(statusKey), 1L);
            return computeTyLeHoanThanhByWorkflow(one, flowBuoc, 1L);
        }
        return 0D;
    }

    /** Tỷ lệ gom tỉnh/khu vực — ưu tiên luồng (khớp cột HĐ), rồi sản lượng/kế hoạch, rồi đếm HT. */
    public static double resolveAggregateTyLeHoanThanh(
            BigDecimal constructionValue,
            BigDecimal contractValue,
            Map<String, Long> statusByMa,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            long soDoiTuong) {
        if (flowBuoc != null && !flowBuoc.isEmpty() && soDoiTuong > 0) {
            return computeDisplayTyLeHoanThanh(statusByMa, soDoiTuong, flowBuoc);
        }
        BigDecimal construction = VolumeTinhToanHelper.nz(constructionValue);
        BigDecimal contract = VolumeTinhToanHelper.nz(contractValue);
        if (contract.compareTo(BigDecimal.ZERO) > 0) {
            return VolumeTinhToanHelper.ratio(construction, contract).doubleValue();
        }
        return computeTyLeHoanThanh(statusByMa);
    }

    private static boolean isNonProgressFlowStep(LuongTrangThaiBuocResponse step) {
        if (step.getTrangThaiHopDongId() == null) {
            return true;
        }
        if (isHuyStatus(step.getMa(), step.getTen())) {
            return true;
        }
        if (isVuongMacStatus(step.getMa(), step.getTen())) {
            return true;
        }
        if (isPendingCancelStatus(step.getMa(), step.getTen())) {
            return true;
        }
        if (isPendingCompleteStatus(step.getMa(), step.getTen())) {
            return true;
        }
        return step.getMa() != null && isCompletedStatus(step.getMa());
    }
}
