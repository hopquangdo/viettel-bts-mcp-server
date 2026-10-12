package vn.edu.huce.iic.bts_ops_platform.mcp.common.ky;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KyBaoCao {

    private KyBaoCao() {
    }

    public record KhoangNgay(LocalDate fromDate, LocalDate toDate) {
        public KhoangNgay {
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                LocalDate tmp = fromDate;
                fromDate = toDate;
                toDate = tmp;
            }
        }

        public Instant tuInstant() {
            return fromDate == null ? null : fromDate.atStartOfDay(VietnamDateUtils.ZONE).toInstant();
        }

        public Instant denInstantKhongBaoGom() {
            return toDate == null ? null : toDate.plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant();
        }
    }

    public record KyTruoc(KhoangNgay khoangNgay, String nhan) {
    }

    public static KhoangNgay chuanHoa(LocalDate fromDate, LocalDate toDate,
                                     LocalDate defaultFromDate, LocalDate defaultToDate) {
        return new KhoangNgay(fromDate != null ? fromDate : defaultFromDate,
                toDate != null ? toDate : defaultToDate);
    }

    public static KyTruoc kyTruoc(KhoangNgay khoang) {
        if (khoang.fromDate() == null || khoang.toDate() == null) {
            throw new IllegalArgumentException("Kỳ báo cáo cần có đủ fromDate và toDate");
        }

        long days = ChronoUnit.DAYS.between(khoang.fromDate(), khoang.toDate()) + 1;
        if (days <= 1) {
            return new KyTruoc(new KhoangNgay(khoang.fromDate().minusDays(1), khoang.toDate().minusDays(1)), "Hôm qua");
        }
        if (days <= 7) {
            return new KyTruoc(new KhoangNgay(khoang.fromDate().minusWeeks(1), khoang.toDate().minusWeeks(1)), "Tuần trước");
        }
        if (days <= 31) {
            return new KyTruoc(new KhoangNgay(khoang.fromDate().minusMonths(1), khoang.toDate().minusMonths(1)), "Tháng trước");
        }
        if (days <= 366) {
            return new KyTruoc(new KhoangNgay(khoang.fromDate().minusYears(1), khoang.toDate().minusYears(1)), "Cùng kỳ năm trước");
        }
        return new KyTruoc(new KhoangNgay(khoang.fromDate().minusDays(days), khoang.toDate().minusDays(days)), "Kỳ trước liền kề");
    }

    public static List<KhoangNgay> chiaO(KhoangNgay khoang, String chiaTheo) {
        if (khoang.fromDate() == null || khoang.toDate() == null) {
            throw new IllegalArgumentException("Chia kỳ cần có đủ fromDate và toDate");
        }

        String donVi = chiaTheo == null ? "thang" : chiaTheo.trim().toLowerCase(Locale.ROOT);
        List<KhoangNgay> ketQua = new ArrayList<>();
        LocalDate cursor = khoang.fromDate();
        while (!cursor.isAfter(khoang.toDate())) {
            LocalDate dauO = dauO(cursor, donVi);
            LocalDate cuoiO = cuoiO(dauO, donVi);
            LocalDate tu = dauO.isBefore(khoang.fromDate()) ? khoang.fromDate() : dauO;
            LocalDate den = cuoiO.isAfter(khoang.toDate()) ? khoang.toDate() : cuoiO;
            ketQua.add(new KhoangNgay(tu, den));
            cursor = den.plusDays(1);
        }
        return List.copyOf(ketQua);
    }

    private static LocalDate dauO(LocalDate date, String donVi) {
        return switch (donVi) {
            case "ngay", "day" -> date;
            case "tuan", "week" -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "thang", "month" -> date.withDayOfMonth(1);
            case "quy", "quarter" -> LocalDate.of(date.getYear(), ((date.getMonthValue() - 1) / 3) * 3 + 1, 1);
            case "nam", "year" -> date.withDayOfYear(1);
            default -> throw new IllegalArgumentException("chiaTheo không hợp lệ: " + donVi);
        };
    }

    private static LocalDate cuoiO(LocalDate dauO, String donVi) {
        return switch (donVi) {
            case "ngay", "day" -> dauO;
            case "tuan", "week" -> dauO.plusWeeks(1).minusDays(1);
            case "thang", "month" -> dauO.plusMonths(1).minusDays(1);
            case "quy", "quarter" -> dauO.plusMonths(3).minusDays(1);
            case "nam", "year" -> dauO.plusYears(1).minusDays(1);
            default -> throw new IllegalArgumentException("chiaTheo không hợp lệ: " + donVi);
        };
    }
}