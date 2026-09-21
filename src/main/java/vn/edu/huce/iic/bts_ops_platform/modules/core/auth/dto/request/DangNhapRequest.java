package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DangNhapRequest {

    @NotBlank
    @Size(max = 100)
    private String tenDangNhap;

    @NotBlank
    @Size(min = 6, max = 100)
    private String matKhau;
}
