package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChuDauTuTaoRequest {
    @NotBlank
    @Size(max = 50)
    private String ma;
    @NotBlank
    @Size(max = 255)
    private String ten;
    @Size(max = 2000)
    private String moTa;
    private Boolean hoatDong = true;
}
