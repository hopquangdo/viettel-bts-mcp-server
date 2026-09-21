package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTheoLinhVucRegionResponse {
    private UUID khuVucId;
    private String ma;
    private String ten;
    @Builder.Default
    private List<DashboardTheoLinhVucItemResponse> domains = new ArrayList<>();
}
