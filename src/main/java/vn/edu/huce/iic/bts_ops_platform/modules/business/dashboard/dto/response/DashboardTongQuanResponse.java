package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTongQuanResponse {
    private Instant capNhatLuc;
    private long tongDoiTuong;
    private long soHopDong;
    private long soVuongMacDangVuong;
    private long soKhuVuc;
    private DashboardSanLuongMetricResponse sanLuongNam;
    private DashboardSanLuongMetricResponse sanLuongThang;
}
