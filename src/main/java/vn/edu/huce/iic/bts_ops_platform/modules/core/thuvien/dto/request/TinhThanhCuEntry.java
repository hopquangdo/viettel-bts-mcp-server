package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class TinhThanhCuEntry {

    private UUID id;

    @NotBlank
    @Size(max = 20)
    private String ma;

    @NotBlank
    @Size(max = 255)
    private String ten;
}
