package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class KieuHopDongTaoRequest {
    @NotNull
    private UUID loaiHopDongId;
    @NotBlank
    @Size(max = 100)
    private String ma;
    @NotBlank
    @Size(max = 255)
    private String ten;
    @NotBlank
    @Size(max = 1)
    private String nhom;
    @Size(max = 20)
    private String mauSac;
    @Size(max = 2000)
    private String moTa;
    private Boolean hoatDong = true;
}
