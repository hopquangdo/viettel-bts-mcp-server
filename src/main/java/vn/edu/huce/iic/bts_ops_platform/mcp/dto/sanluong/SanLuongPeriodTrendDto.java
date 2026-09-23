package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 1 kỳ con của xu hướng: giá trị sản lượng hoàn thành trong kỳ con (không có tỷ lệ hoàn thành theo kỳ). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongPeriodTrendDto {

    private String period;
    private BigDecimal totalValue;
    private Integer totalObjects;
    private Integer openIssueCount;
}
