package vn.edu.huce.iic.bts_ops_platform.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Bucket tiến độ hoàn thành trung bình — dùng chung cho mọi tool OVERVIEW có khái niệm "% hoàn thành". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSummaryDto {
    private BigDecimal completionRate;
    private int completedCount;
    private int inProgressCount;
    private int notStartedCount;
    private int lowCompletionCount;
}
