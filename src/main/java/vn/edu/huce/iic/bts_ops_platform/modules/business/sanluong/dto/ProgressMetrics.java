package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProgressMetrics(
        int itemsDone,
        int itemsTotal,
        int completionPercent,
        int issueCount,
        BigDecimal totalOutput,
        BigDecimal todayOutput,
        Instant lastUpdated) {
}
