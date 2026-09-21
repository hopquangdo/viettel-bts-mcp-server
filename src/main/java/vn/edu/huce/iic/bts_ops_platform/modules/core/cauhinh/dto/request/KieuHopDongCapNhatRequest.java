package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class KieuHopDongCapNhatRequest {
    private UUID loaiHopDongId;
    @Size(max = 100)
    private String ma;
    @Size(max = 255)
    private String ten;
    @Size(max = 1)
    private String nhom;
    @Size(max = 20)
    private String mauSac;
    @Size(max = 2000)
    private String moTa;
    private Boolean hoatDong;
}
