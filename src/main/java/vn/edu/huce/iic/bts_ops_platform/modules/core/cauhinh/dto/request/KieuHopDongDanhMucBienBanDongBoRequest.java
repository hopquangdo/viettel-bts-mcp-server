package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class KieuHopDongDanhMucBienBanDongBoRequest {
    @NotNull
    private UUID kieuHopDongId;
    private List<UUID> danhMucBienBanIds;
}
