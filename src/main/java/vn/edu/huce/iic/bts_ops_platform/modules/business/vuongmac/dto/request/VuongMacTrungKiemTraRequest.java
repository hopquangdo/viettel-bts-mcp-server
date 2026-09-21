package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class VuongMacTrungKiemTraRequest {
    @NotEmpty
    private List<UUID> duLieuDoiTuongIds;

    @NotBlank
    private String giaiDoan;
}
