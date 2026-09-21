package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhanAnhTaoRequest {

    @NotBlank
    @Pattern(regexp = "LOI_HE_THONG|VUONG_MAC|DE_XUAT", message = "Loại phải là LOI_HE_THONG, VUONG_MAC hoặc DE_XUAT")
    private String loai;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String tieuDe;

    private String noiDung;
}
