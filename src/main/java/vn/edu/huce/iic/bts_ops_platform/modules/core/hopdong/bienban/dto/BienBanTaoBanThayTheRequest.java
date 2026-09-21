package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BienBanTaoBanThayTheRequest {
    @NotBlank
    private String lyDo;
}
