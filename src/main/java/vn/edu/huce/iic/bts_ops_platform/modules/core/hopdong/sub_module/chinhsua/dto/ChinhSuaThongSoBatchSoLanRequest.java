package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChinhSuaThongSoBatchSoLanRequest {

    @NotEmpty
    private List<UUID> hopDongDoiTuongIds;
}
