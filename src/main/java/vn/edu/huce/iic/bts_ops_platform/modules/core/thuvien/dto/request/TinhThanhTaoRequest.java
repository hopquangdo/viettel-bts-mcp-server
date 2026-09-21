package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class TinhThanhTaoRequest {

    @NotBlank
    @Size(max = 20)
    private String ma;

    @NotBlank
    @Size(max = 255)
    private String ten;

    @NotNull
    private UUID khuVucId;

    /** Gắn vào nhóm tỉnh đã có (sau sáp nhập). Bỏ trống để tạo nhóm mới. */
    private UUID tinhThanhId;

    private Boolean laTinhCu = false;

    private Boolean hoatDong = true;
}
