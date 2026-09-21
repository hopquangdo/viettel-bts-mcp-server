package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SanLuongResponse {
    private UUID id;
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
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private String ketQuaNghiemThu;
    private String lyDoKhongDat;
    private UUID nguoiNghiemThuId;
    private String nguoiNghiemThuTen;
    private Instant ngayNghiemThu;
}
