package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VatTuTrangThaiCapNhatRequest {

    @NotBlank
    private String trangThai;

    private LocalDate ngayHoanThanh;
    private String ghiChu;
}
