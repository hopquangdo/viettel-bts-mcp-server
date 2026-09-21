package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SoLanSuaThongSoItemResponse {

    private UUID thuocTinhId;
    private int soLanDaApDung;
    private boolean choDuyet;
}
