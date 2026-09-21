package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TrangThaiHopDongTaoRequest {
    @Size(max = 20)
    private String ten;
    @Size(max = 20)
    private String ma;
    @Size(max = 20)
    private String mauSac;
    private Boolean hoatDong = true;
}
