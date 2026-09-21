package vn.edu.huce.iic.bts_ops_platform.dto.vuongmac;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;

import java.time.Instant;
import java.util.UUID;

/** 1 bản ghi vướng mắc/sự cố — dùng cho vuongmac_search/vuongmac_qua_han. */
@Data
public class VuongMacItemDto {
    private UUID id;
    private String ma;
    private HopDongInfo hopDong;
    private String giaiDoan;
    private String kieuVuongMac;
    private Boolean coTheBoSungSanLuong;
    private String moTa;
    private String moTaDayDu;
    private String tenNguoiBaoCao;
    private String trangThai;
    private String tenNguoiXuLy;
    private Boolean quaHan30Ngay;
    private String tenLoaiHopDong;
    private String khuVuc;
    private String tinh;
    private String nhaThau;
    private Boolean hoatDong;
    private Instant ngayTao;
}
