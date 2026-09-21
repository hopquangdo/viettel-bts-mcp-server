package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.edu.huce.iic.bts_ops_platform.common.security.PasswordPolicy;
import lombok.Data;

import java.util.UUID;

@Data
public class DangKyRequest {

    @NotNull
    private UUID quyenId;

    @NotNull
    private UUID khuVucId;

    @NotBlank
    @Size(max = 100)
    private String tenDangNhap;

    @NotBlank
    @Size(min = 6, max = 100)
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String matKhau;

    @NotBlank
    @Size(max = 255)
    private String hoTen;

    @Size(max = 20)
    private String dienThoai;

    @Email
    @Size(max = 255)
    private String email;
}
