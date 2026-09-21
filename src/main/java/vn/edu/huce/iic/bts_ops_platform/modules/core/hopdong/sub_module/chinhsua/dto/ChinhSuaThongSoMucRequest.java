package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChinhSuaThongSoMucRequest {

    @NotNull
    private UUID thuocTinhId;

    private String giaTriMoi;
}
