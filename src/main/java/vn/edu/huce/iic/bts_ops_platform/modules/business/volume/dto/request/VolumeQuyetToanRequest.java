package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VolumeQuyetToanRequest {
    /** null = xóa quyết toán */
    private BigDecimal quyetToanThuc;
}
