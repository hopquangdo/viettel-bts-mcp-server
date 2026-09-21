package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LamMoiTokenRequest {

    @NotBlank
    private String refreshToken;
}
