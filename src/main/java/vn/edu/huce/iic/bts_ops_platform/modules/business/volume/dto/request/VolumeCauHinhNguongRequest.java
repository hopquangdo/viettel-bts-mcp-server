package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
public class VolumeCauHinhNguongRequest {

    /** Hệ số mặc định cho loại GCCC (× bình quân/trạm). */
    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "Hệ số GCCC phải lớn hơn 0")
    private BigDecimal gcccHeSo;

    /** Hệ số cho loại Xây mới & Còn lại (× bình quân/trạm). */
    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "Hệ số Xây mới phải lớn hơn 0")
    private BigDecimal xayMoiHeSo;

    /** Hệ số riêng theo hợp đồng GCCC — hopDongId → hệ số. */
    private Map<UUID, BigDecimal> overrides;
}
