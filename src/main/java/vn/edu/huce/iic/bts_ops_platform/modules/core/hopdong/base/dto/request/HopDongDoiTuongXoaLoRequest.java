package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HopDongDoiTuongXoaLoRequest {

    @NotEmpty
    private List<UUID> ids;
}
