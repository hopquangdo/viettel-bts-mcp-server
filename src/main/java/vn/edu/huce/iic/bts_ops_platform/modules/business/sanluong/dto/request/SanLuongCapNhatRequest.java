package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SanLuongCapNhatRequest {
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private UUID hangMucCongViecId;
    private UUID hangMucChiTietId;
    private LocalDate ngayThucHien;
    private BigDecimal donGia;
    private BigDecimal khoiLuongHoanThanh;
    private String trangThai;
    private String ghiChu;
    private Boolean hoatDong;
}
