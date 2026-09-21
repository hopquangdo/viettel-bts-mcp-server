package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoaiHopDongCapNhatRequest {
    @Size(max = 50)
    private String ma;
    @Size(max = 255)
    private String ten;
    @Size(max = 2000)
    private String moTa;
    @DecimalMin(value = "0", inclusive = false, message = "Hệ số phải lớn hơn 0")
    private BigDecimal hesoNguong;
    private String heNghiepVu;
    private Boolean hoatDong;
}
