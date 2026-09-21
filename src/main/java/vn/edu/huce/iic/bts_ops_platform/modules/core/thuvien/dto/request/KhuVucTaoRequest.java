package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KhuVucTaoRequest {

    @NotBlank
    @Size(max = 20)
    private String ma;

    @NotBlank
    @Size(max = 255)
    private String ten;

    @Size(max = 500)
    private String moTa;

    private Boolean hoatDong = true;
}
