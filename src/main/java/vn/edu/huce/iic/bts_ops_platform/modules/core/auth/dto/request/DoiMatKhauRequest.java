package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.common.security.PasswordPolicy;

@Data
public class DoiMatKhauRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String matKhauCu;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String matKhauMoi;
}
