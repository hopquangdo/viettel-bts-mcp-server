package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SoLanSuaThongSoResponse {

    private UUID hopDongDoiTuongId;
    private List<SoLanSuaThongSoItemResponse> items;
}
