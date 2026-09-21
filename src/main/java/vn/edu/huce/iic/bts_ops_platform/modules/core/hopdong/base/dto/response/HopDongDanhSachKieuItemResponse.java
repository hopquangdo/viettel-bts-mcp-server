package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HopDongDanhSachKieuItemResponse {

    private UUID id;
    private UUID loaiHopDongId;
    private String ma;
    private String ten;
    private String nhom;
    private String mauSac;
    private String moTa;
    private Boolean hoatDong;
    private long soLuongHopDong;
    private HopDongDanhSachLuongTrangThaiResponse luongTrangThai;
}
