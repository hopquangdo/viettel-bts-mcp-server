package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class HopDongThuocTinhGiaTriItem {
    @NotNull
    private UUID thuocTinhHopDongId;
    private String giaTri;
}
