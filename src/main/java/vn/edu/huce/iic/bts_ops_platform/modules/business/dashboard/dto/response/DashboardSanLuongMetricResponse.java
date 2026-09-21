package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSanLuongMetricResponse {
    private String label;
    private BigDecimal amount;
    private BigDecimal percent;
    /** Tổng thành tiền hạng mục HĐ (định mức). */
    private BigDecimal kpiAmount;
    private String kpiLabel;
}
