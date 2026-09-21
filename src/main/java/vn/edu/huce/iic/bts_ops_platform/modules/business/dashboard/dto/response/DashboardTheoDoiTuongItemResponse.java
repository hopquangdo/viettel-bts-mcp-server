package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTheoDoiTuongItemResponse {
    private UUID doiTuongQuanLyId;
    private String ma;
    private String ten;
    private long soLuong;
}
