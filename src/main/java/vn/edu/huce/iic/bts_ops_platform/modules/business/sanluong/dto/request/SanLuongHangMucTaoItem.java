package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SanLuongHangMucTaoItem {
    private UUID hangMucCongViecId;
    private UUID hangMucChiTietId;
    private BigDecimal donGia;
    private BigDecimal amount;
    private String trangThai;
    private String ghiChu;
}
