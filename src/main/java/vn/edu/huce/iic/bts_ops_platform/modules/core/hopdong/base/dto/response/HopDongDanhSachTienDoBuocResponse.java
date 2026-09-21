package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HopDongDanhSachTienDoBuocResponse {

    private UUID trangThaiHopDongId;
    private String ma;
    private String ten;
    private String mauSac;
    private Short thuTu;
    private long soLuong;
}
