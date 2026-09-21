package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LuongTrangThaiCapNhatRequest {

    @NotBlank
    @Size(max = 100)
    private String ten;

    private String moTa;

    private Boolean hoatDong;
}
