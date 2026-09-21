package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class VolumeCauHinhNguongResponse {
    private BigDecimal gcccHeSo;
    private BigDecimal xayMoiHeSo;
    private Map<UUID, BigDecimal> overrides;
}
