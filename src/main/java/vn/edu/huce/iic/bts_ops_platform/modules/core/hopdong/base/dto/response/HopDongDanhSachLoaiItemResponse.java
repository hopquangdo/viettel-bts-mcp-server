package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HopDongDanhSachLoaiItemResponse {

    private UUID id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean hoatDong;
    private long soLuongHopDong;
    private List<HopDongDanhSachKieuItemResponse> kieuHopDongs;
}
