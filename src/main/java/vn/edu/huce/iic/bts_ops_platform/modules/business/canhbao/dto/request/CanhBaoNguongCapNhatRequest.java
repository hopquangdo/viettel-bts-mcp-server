package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CanhBaoNguongCapNhatRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal giaTriNguong;
}
