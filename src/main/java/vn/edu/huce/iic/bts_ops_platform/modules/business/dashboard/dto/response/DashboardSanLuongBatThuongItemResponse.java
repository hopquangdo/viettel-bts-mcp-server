package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSanLuongBatThuongItemResponse {
    private UUID hopDongDoiTuongId;
    private UUID hopDongId;
    private String maTram;
    private String khuVuc;
    private BigDecimal sanLuong;
    private BigDecimal giaTriBinhQuan;
    private BigDecimal heSo;
    private BigDecimal nguongCanhBao;
    private BigDecimal tyLeSoVoiBinhQuan;
    private boolean daQuyetToan;
    private boolean daKiemTra;
}
