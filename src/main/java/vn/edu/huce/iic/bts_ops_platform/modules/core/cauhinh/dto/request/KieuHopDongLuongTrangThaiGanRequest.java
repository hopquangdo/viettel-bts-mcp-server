package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class KieuHopDongLuongTrangThaiGanRequest {

    @NotNull
    private UUID loaiHopDongId;

    @NotNull
    private UUID kieuHopDongId;

  /** null = gỡ luồng trạng thái */
    private UUID luongTrangThaiId;
}
