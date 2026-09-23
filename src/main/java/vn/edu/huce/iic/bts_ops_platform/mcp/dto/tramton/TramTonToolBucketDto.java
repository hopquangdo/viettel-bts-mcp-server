package vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 1 nhóm (aging/reason) trong tramton_tongquan — {label, count, value, percent}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonToolBucketDto {
    private String label;
    private long count;
    private BigDecimal value;
    private double percent;
}
