package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class HopDongDanhSachTongQuanResponse {

    private long tongHopDongHoatDong;
    private Instant capNhatLuc;
    private List<HopDongDanhSachLoaiItemResponse> loaiHopDongs;
}
