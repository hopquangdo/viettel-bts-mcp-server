package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class HopDongThuocTinhCapNhatRequest {
    private UUID hopDongId;
    @NotNull
    private UUID thuocTinhHopDongId;
    @Size(max = 255)
    private String giaTri;
    private Short thuTu;
    private Boolean hoatDong;
}
