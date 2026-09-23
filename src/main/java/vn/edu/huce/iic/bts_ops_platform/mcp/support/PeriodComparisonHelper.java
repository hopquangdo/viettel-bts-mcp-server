package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Xác định khoảng "cùng kỳ trước" để so sánh — tự nhận diện đơn vị (ngày/tuần/tháng/năm) dựa theo
 * ĐỘ DÀI của khoảng đang xem, rồi lùi lại đúng 1 đơn vị đó. Dùng chung cho mọi tool OVERVIEW cần
 * TrendInfo, để "hôm nay" so với hôm qua, "tuần này" so với tuần trước, "năm nay" so với năm
 * trước... thay vì luôn cố định lùi 1 năm bất kể độ dài kỳ đang xem.
 */
public final class PeriodComparisonHelper {

    private PeriodComparisonHelper() {
    }

    public record PreviousPeriod(LocalDate fromDate, LocalDate toDate, String label) {
    }

    public static PreviousPeriod previousPeriod(LocalDate fromDate, LocalDate toDate) {
        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        if (days <= 1) {
            return new PreviousPeriod(fromDate.minusDays(1), toDate.minusDays(1), "Hôm qua");
        }
        if (days <= 7) {
            return new PreviousPeriod(fromDate.minusWeeks(1), toDate.minusWeeks(1), "Tuần trước");
        }
        if (days <= 31) {
            return new PreviousPeriod(fromDate.minusMonths(1), toDate.minusMonths(1), "Tháng trước");
        }
        if (days <= 366) {
            return new PreviousPeriod(fromDate.minusYears(1), toDate.minusYears(1), "Cùng kỳ năm trước");
        }
        return new PreviousPeriod(fromDate.minusDays(days), toDate.minusDays(days), "Kỳ trước liền kề");
    }
}
