package vn.edu.huce.iic.bts_ops_platform.support;

import vn.edu.huce.iic.bts_ops_platform.dto.common.TrendInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** So sánh giá trị kỳ này với kỳ trước ra {@link TrendInfo} — dùng chung cho mọi tool OVERVIEW cần UP/DOWN/STABLE. */
public final class TrendCalculator {

    private TrendCalculator() {
    }

    public static TrendInfo compare(BigDecimal current, BigDecimal previous, String comparisonLabel) {
        String direction;
        BigDecimal growthPercent;
        if (previous.signum() == 0) {
            direction = current.signum() == 0 ? "NO_DATA" : "UP";
            growthPercent = current.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        } else {
            growthPercent = current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            if (growthPercent.signum() > 0) {
                direction = "UP";
            } else if (growthPercent.signum() < 0) {
                direction = "DOWN";
            } else {
                direction = "STABLE";
            }
        }
        return new TrendInfo(current, previous, growthPercent.setScale(1, RoundingMode.HALF_UP), direction, comparisonLabel);
    }
}
