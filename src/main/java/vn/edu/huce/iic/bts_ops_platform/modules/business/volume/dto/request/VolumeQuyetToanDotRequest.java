package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VolumeQuyetToanDotRequest {
    private BigDecimal soTien;
    private LocalDate ngayQuyetToan;
    private String ghiChu;
}
