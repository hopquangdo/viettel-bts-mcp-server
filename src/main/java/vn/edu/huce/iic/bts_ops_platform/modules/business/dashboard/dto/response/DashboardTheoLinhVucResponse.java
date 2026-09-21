package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTheoLinhVucResponse {
    private String timeMode;
    private Integer nam;
    private Integer thang;
    private Integer tuan;
    private Integer quy;
    private String ngay;
    @Builder.Default
    private List<DashboardTheoLinhVucRegionResponse> regions = new ArrayList<>();
}
