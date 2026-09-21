package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.PeriodScope;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.ThuocTinhLookup;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardSanLuongMetricResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuThePointResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuTheSeriesResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Các hàm tính toán/định dạng thuần túy cho dashboard (không phụ thuộc service). */
public final class DashboardTinhToanHelper {

    private static final String KPI_CHUA_CO = "Chưa có dữ liệu KPI";
    private static final BigDecimal TY = new BigDecimal("1000000000");
    private static final int YEAR_LOOKBACK = 5;

    private DashboardTinhToanHelper() {
    }

    public static DashboardSanLuongMetricResponse metric(String label, BigDecimal amount) {
        return metric(label, amount, null, null);
    }

    /**
     * @param previousAmount kỳ trước (năm trước / tháng trước); null hoặc 0 → % = 0 nếu amount=0, 100 nếu amount&gt;0
     */
    public static DashboardSanLuongMetricResponse metric(
            String label,
            BigDecimal amount,
            BigDecimal previousAmount) {
        return metric(label, amount, previousAmount, null);
    }

    public static DashboardSanLuongMetricResponse metric(
            String label,
            BigDecimal amount,
            BigDecimal previousAmount,
            BigDecimal kpiAmount) {
        BigDecimal current = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal kpi = kpiAmount != null ? kpiAmount : BigDecimal.ZERO;
        return DashboardSanLuongMetricResponse.builder()
                .label(label)
                .amount(current)
                .percent(growthPercent(current, previousAmount))
                .kpiAmount(kpi)
                .kpiLabel(formatKpiLabel(kpi))
                .build();
    }

    public static String formatKpiLabel(BigDecimal kpiAmount) {
        BigDecimal kpi = kpiAmount != null ? kpiAmount : BigDecimal.ZERO;
        if (kpi.compareTo(BigDecimal.ZERO) == 0) {
            return KPI_CHUA_CO;
        }
        BigDecimal ty = kpi.divide(TY, 2, RoundingMode.HALF_UP);
        String formatted = ty.toPlainString().replace('.', ',');
        return "Tổng KPI: " + formatted + " tỉ đồng";
    }

    public static BigDecimal completionPercent(BigDecimal actual, BigDecimal kpi) {
        BigDecimal cur = actual != null ? actual : BigDecimal.ZERO;
        BigDecimal target = kpi != null ? kpi : BigDecimal.ZERO;
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return cur.multiply(new BigDecimal("100"))
                .divide(target, 1, RoundingMode.HALF_UP);
    }

    /** % tăng trưởng = (hiện tại − kỳ trước) / kỳ trước × 100. */
    public static BigDecimal growthPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = current != null ? current : BigDecimal.ZERO;
        BigDecimal prev = previous != null ? previous : BigDecimal.ZERO;
        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            if (cur.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal("100");
        }
        return cur.subtract(prev)
                .multiply(new BigDecimal("100"))
                .divide(prev, 1, RoundingMode.HALF_UP);
    }

    public static DashboardXuTheSeriesResponse buildXuTheSeries(
            UUID khuVucId,
            String ma,
            String ten,
            List<String> buckets,
            Map<String, BigDecimal> byBucket) {
        return buildXuTheSeries(khuVucId, ma, ten, buckets, byBucket, BigDecimal.ZERO);
    }

    public static DashboardXuTheSeriesResponse buildXuTheSeries(
            UUID khuVucId,
            String ma,
            String ten,
            List<String> buckets,
            Map<String, BigDecimal> byBucket,
            BigDecimal kpiAmount) {
        List<DashboardXuThePointResponse> points = new ArrayList<>();
        for (String bucket : buckets) {
            points.add(DashboardXuThePointResponse.builder()
                    .bucket(bucket)
                    .amount(byBucket.getOrDefault(bucket, BigDecimal.ZERO))
                    .build());
        }
        return DashboardXuTheSeriesResponse.builder()
                .khuVucId(khuVucId)
                .ma(ma)
                .ten(ten)
                .points(points)
                .kpiAmount(kpiAmount != null ? kpiAmount : BigDecimal.ZERO)
                .build();
    }

    public static PeriodScope resolveXuTheScope(String mode, int year, int month) {
        // Tương thích cũ: granularity đồng thời là phạm vi + bucket
        LocalDate dateFrom;
        LocalDate dateTo;
        switch (mode) {
            case "month" -> {
                dateFrom = LocalDate.of(year, 1, 1);
                dateTo = LocalDate.of(year, 12, 31);
            }
            case "year" -> {
                int fromYear = year - YEAR_LOOKBACK + 1;
                dateFrom = LocalDate.of(fromYear, 1, 1);
                dateTo = LocalDate.of(year, 12, 31);
            }
            case "day" -> {
                dateFrom = LocalDate.of(year, month, 1);
                dateTo = YearMonth.of(year, month).atEndOfMonth();
            }
            case "quarter" -> {
                dateFrom = LocalDate.of(year, 1, 1);
                dateTo = LocalDate.of(year, 12, 31);
            }
            default -> {
                YearMonth ym = YearMonth.of(year, month);
                dateFrom = ym.atDay(1);
                dateTo = ym.atEndOfMonth();
            }
        }
        return new PeriodScope(dateFrom, dateTo, buildBuckets(mode, dateFrom, dateTo));
    }

    /**
     * Phạm vi ngày theo filter UI (timeMode + ngay/tuan/thang/quy/nam).
     */
    public static PeriodScope resolveScopeFromTimeMode(
            String timeMode,
            String granularity,
            int year,
            Integer thang,
            Integer tuan,
            Integer quy,
            LocalDate ngay) {
        int month = thang != null ? thang : LocalDate.now().getMonthValue();
        LocalDate dateFrom;
        LocalDate dateTo;
        String mode = timeMode == null ? "month" : timeMode;

        switch (mode) {
            case "day" -> {
                LocalDate d = ngay != null ? ngay : LocalDate.of(year, month, Math.min(LocalDate.now().getDayOfMonth(),
                        YearMonth.of(year, month).lengthOfMonth()));
                dateFrom = d;
                dateTo = d;
            }
            case "week" -> {
                int week = tuan != null && tuan > 0 ? tuan : 1;
                YearMonth ym = YearMonth.of(year, month);
                int startDay = (week - 1) * 7 + 1;
                int endDay = Math.min(week * 7, ym.lengthOfMonth());
                if (startDay > ym.lengthOfMonth()) {
                    startDay = ym.lengthOfMonth();
                    endDay = startDay;
                }
                dateFrom = ym.atDay(startDay);
                dateTo = ym.atDay(endDay);
            }
            case "quarter" -> {
                int q = quy != null && quy >= 1 && quy <= 4 ? quy : ((month - 1) / 3) + 1;
                int startMonth = (q - 1) * 3 + 1;
                dateFrom = LocalDate.of(year, startMonth, 1);
                dateTo = YearMonth.of(year, startMonth + 2).atEndOfMonth();
            }
            case "year" -> {
                dateFrom = LocalDate.of(year, 1, 1);
                dateTo = LocalDate.of(year, 12, 31);
            }
            default -> { // month
                YearMonth ym = YearMonth.of(year, month);
                dateFrom = ym.atDay(1);
                dateTo = ym.atEndOfMonth();
            }
        }

        String bucketMode = granularity == null || granularity.isBlank() ? mode : granularity;
        return new PeriodScope(dateFrom, dateTo, buildBuckets(bucketMode, dateFrom, dateTo));
    }

    public static List<String> buildBuckets(String granularity, LocalDate dateFrom, LocalDate dateTo) {
        List<String> buckets = new ArrayList<>();
        if (dateFrom == null || dateTo == null || dateFrom.isAfter(dateTo)) {
            return buckets;
        }
        String mode = granularity == null ? "week" : granularity;
        switch (mode) {
            case "day" -> {
                for (LocalDate d = dateFrom; !d.isAfter(dateTo); d = d.plusDays(1)) {
                    buckets.add(formatDayBucket(d));
                }
            }
            case "month" -> {
                YearMonth from = YearMonth.from(dateFrom);
                YearMonth to = YearMonth.from(dateTo);
                boolean multiYear = from.getYear() != to.getYear();
                for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
                    buckets.add(multiYear
                            ? "T" + ym.getMonthValue() + "/" + ym.getYear()
                            : "T" + ym.getMonthValue());
                }
            }
            case "quarter" -> {
                int fromQ = quarterOf(dateFrom);
                int toQ = quarterOf(dateTo);
                int y = dateFrom.getYear();
                int endY = dateTo.getYear();
                boolean multiYear = y != endY;
                for (int year = y; year <= endY; year++) {
                    int qStart = year == y ? fromQ : 1;
                    int qEnd = year == endY ? toQ : 4;
                    for (int q = qStart; q <= qEnd; q++) {
                        buckets.add(multiYear ? "Q" + q + "/" + year : "Q" + q);
                    }
                }
            }
            case "year" -> {
                for (int year = dateFrom.getYear(); year <= dateTo.getYear(); year++) {
                    buckets.add(String.valueOf(year));
                }
            }
            default -> { // week — tuần trong tháng (ngày 1–7 = Tuần 1, …)
                YearMonth from = YearMonth.from(dateFrom);
                YearMonth to = YearMonth.from(dateTo);
                for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
                    LocalDate monthStart = ym.atDay(1);
                    LocalDate monthEnd = ym.atEndOfMonth();
                    LocalDate rangeStart = dateFrom.isAfter(monthStart) ? dateFrom : monthStart;
                    LocalDate rangeEnd = dateTo.isBefore(monthEnd) ? dateTo : monthEnd;
                    int startWeek = (rangeStart.getDayOfMonth() - 1) / 7 + 1;
                    int endWeek = (rangeEnd.getDayOfMonth() - 1) / 7 + 1;
                    boolean multiMonth = !from.equals(to);
                    for (int w = startWeek; w <= endWeek; w++) {
                        buckets.add(multiMonth
                                ? formatWeekBucket(ym.getMonthValue(), w)
                                : "Tuần " + w);
                    }
                }
            }
        }
        return buckets;
    }

    public static String toBucketKey(String mode, LocalDate ngay, int year, int month) {
        // Giữ tương thích cũ (scope năm/tháng cố định)
        return switch (mode) {
            case "day" -> formatDayBucket(ngay);
            case "month" -> ngay.getYear() == year ? "T" + ngay.getMonthValue() : null;
            case "quarter" -> ngay.getYear() == year ? "Q" + quarterOf(ngay) : null;
            case "year" -> String.valueOf(ngay.getYear());
            default -> {
                if (ngay.getYear() != year || ngay.getMonthValue() != month) {
                    yield null;
                }
                yield "Tuần " + ((ngay.getDayOfMonth() - 1) / 7 + 1);
            }
        };
    }

    /** Gán bucket theo granularity trong khoảng dateFrom–dateTo (filter UI). */
    public static String toBucketKeyInRange(
            String granularity,
            LocalDate ngay,
            LocalDate dateFrom,
            LocalDate dateTo) {
        if (ngay == null || dateFrom == null || dateTo == null) {
            return null;
        }
        if (ngay.isBefore(dateFrom) || ngay.isAfter(dateTo)) {
            return null;
        }
        String mode = granularity == null ? "week" : granularity;
        return switch (mode) {
            case "day" -> formatDayBucket(ngay);
            case "month" -> {
                boolean multiYear = dateFrom.getYear() != dateTo.getYear();
                YearMonth ym = YearMonth.from(ngay);
                yield multiYear
                        ? "T" + ym.getMonthValue() + "/" + ym.getYear()
                        : "T" + ym.getMonthValue();
            }
            case "quarter" -> {
                boolean multiYear = dateFrom.getYear() != dateTo.getYear();
                int q = quarterOf(ngay);
                yield multiYear ? "Q" + q + "/" + ngay.getYear() : "Q" + q;
            }
            case "year" -> String.valueOf(ngay.getYear());
            default -> {
                boolean multiMonth = !YearMonth.from(dateFrom).equals(YearMonth.from(dateTo));
                int w = (ngay.getDayOfMonth() - 1) / 7 + 1;
                yield multiMonth ? formatWeekBucket(ngay.getMonthValue(), w) : "Tuần " + w;
            }
        };
    }

    private static int quarterOf(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    private static String formatDayBucket(LocalDate d) {
        return String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue());
    }

    private static String formatWeekBucket(int month, int week) {
        return "T" + month + "-Tuần " + week;
    }

    public static boolean isKhuVucThuocTinh(ThuocTinhResponse thuocTinh) {
        if ("khu_vuc".equalsIgnoreCase(thuocTinh.getKieuDuLieuId())) {
            return true;
        }
        String ten = TextUtils.normalizeLower(thuocTinh.getTen());
        return ten.contains("khu vuc") || ten.contains("region");
    }

    public static boolean isTinhThanhThuocTinh(ThuocTinhResponse thuocTinh) {
        if ("tinh_thanh".equalsIgnoreCase(thuocTinh.getKieuDuLieuId())) {
            return true;
        }
        String ten = TextUtils.normalizeLower(thuocTinh.getTen());
        return ten.contains("tinh thanh") || (ten.contains("tinh") && !ten.contains("tinh cu"));
    }

    public static String resolveMaTram(ThuocTinhLookup lookup, Map<UUID, String> giaTri) {
        if (lookup != null && lookup.primaryAttrId() != null) {
            String value = giaTri.get(lookup.primaryAttrId());
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "—";
    }

    public static String resolveKhuVuc(
            ThuocTinhLookup lookup,
            Map<UUID, String> giaTri,
            Map<UUID, String> khuVucById) {
        if (lookup == null || lookup.khuVucAttrId() == null) {
            return "—";
        }
        String raw = giaTri.get(lookup.khuVucAttrId());
        if (raw == null || raw.isBlank()) {
            return "—";
        }
        UUID linked = UuidUtils.parseUuid(raw);
        if (linked != null) {
            String name = khuVucById.get(linked);
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return raw.trim();
    }

}
