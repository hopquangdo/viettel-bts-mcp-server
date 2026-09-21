package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChiTietTinhItemResponse {
    private UUID tinhThanhId;
    private String ma;
    private String ten;
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal kpiAmount = BigDecimal.ZERO;
}
