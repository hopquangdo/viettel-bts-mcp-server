package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class KeHoachTrienKhaiTaoRequest {

    @NotNull
    private UUID hopDongId;

    @NotBlank
    private String ten;
}
