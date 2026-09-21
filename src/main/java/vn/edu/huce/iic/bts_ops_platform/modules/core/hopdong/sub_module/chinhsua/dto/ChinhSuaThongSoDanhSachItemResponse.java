package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChinhSuaThongSoDanhSachItemResponse {

    private ChinhSuaThongSoResponse deXuat;
    private String maTram;
    private String tenHopDong;
    private String maHopDong;
}
