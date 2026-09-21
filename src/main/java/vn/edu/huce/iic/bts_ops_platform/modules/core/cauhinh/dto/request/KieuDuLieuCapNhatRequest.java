package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class KieuDuLieuCapNhatRequest {
    @Size(max = 20)
    private String ten;
    @Size(max = 100)
    private String lienKetBang;
    private Boolean hoatDong;
}
