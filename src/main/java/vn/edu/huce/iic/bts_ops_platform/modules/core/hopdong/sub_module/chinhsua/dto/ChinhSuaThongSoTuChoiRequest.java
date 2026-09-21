package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChinhSuaThongSoTuChoiRequest {

    @NotBlank
    private String lyDo;
}
