package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

import java.math.BigDecimal;

/** So sánh 1 chỉ số với kỳ trước — dùng chung cho mọi tool cần "so với hôm qua/cùng kỳ năm trước". */
public record TrendInfo(
        BigDecimal currentValue,
        BigDecimal previousValue,
        BigDecimal growthPercent,
        String direction,
        String comparisonLabel
) {
}
