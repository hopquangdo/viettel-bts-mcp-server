package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KpiNguongCapNhatRequest {

    private String ten;

    @NotNull
    private Integer soNgayNguong;

    private Integer uuTien;
    private Boolean hoatDong;
}
