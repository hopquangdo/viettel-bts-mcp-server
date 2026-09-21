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
public class DashboardXuTheSanLuongResponse {
    /** Phạm vi filter UI: day | week | month | quarter | year */
    private String timeMode;
    /** Độ chia bucket: day | week | month | quarter | year */
    private String granularity;
    private Integer nam;
    private Integer thang;
    private Integer tuan;
    private Integer quy;
    private String ngay;
    @Builder.Default
    private List<String> buckets = new ArrayList<>();
    @Builder.Default
    private List<DashboardXuTheSeriesResponse> series = new ArrayList<>();
}
