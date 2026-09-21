package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardXuTheSeriesResponse {
    private UUID khuVucId;
    private String ma;
    private String ten;
    @Builder.Default
    private List<DashboardXuThePointResponse> points = new ArrayList<>();
    @Builder.Default
    private BigDecimal kpiAmount = BigDecimal.ZERO;
}
