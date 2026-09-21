package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PhanCongTramItemResponse {
    private UUID id;
    private UUID hopDongId;
    private String maTram;
    private String maHopDong;
    private UUID nhaThauId;
    private String nhaThau;
    private String giaiDoan;
    private String giaiDoanMa;
    private String maVung;
    private String tinhThanh;
    private BigDecimal sanLuong;
    private double tienDoPercent;
    private boolean ton;
    private boolean vuongMac;
    private boolean hoanThanh;
    private LocalDate ngayHtTc;
    private BigDecimal quyetToanThuc;
}
