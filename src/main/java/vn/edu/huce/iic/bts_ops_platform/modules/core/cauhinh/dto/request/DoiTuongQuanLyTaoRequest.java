package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoiTuongQuanLyTaoRequest {
    @NotBlank
    @Size(max = 50)
    private String ma;
    @NotBlank
    @Size(max = 255)
    private String ten;
    @Size(max = 20)
    private String bieuTuong;
    @Size(max = 2000)
    private String moTa;
    private Boolean hoatDong = true;
    private Boolean hienThiTrenGiaoDien = true;
}
