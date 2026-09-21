package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LuongTrangThaiDongBoRequest {

    @NotNull
    private List<UUID> trangThaiHopDongIds;
}
