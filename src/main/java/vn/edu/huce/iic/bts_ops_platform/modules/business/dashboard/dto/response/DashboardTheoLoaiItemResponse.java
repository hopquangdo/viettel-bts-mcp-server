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
public class DashboardTheoLoaiItemResponse {
    private UUID loaiHopDongId;
    private String ma;
    private String ten;
    private long soHopDong;
    private long soDoiTuong;
    private BigDecimal tongSanLuong;
    private long soVuongMacDangVuong;
    @Builder.Default
    private List<DashboardTheoDoiTuongItemResponse> theoDoiTuong = new ArrayList<>();
}
