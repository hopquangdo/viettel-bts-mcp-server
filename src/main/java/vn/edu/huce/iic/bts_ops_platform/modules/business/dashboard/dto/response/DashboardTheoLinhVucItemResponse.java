package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTheoLinhVucItemResponse {
    private UUID loaiHopDongId;
    private String ma;
    private String ten;
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    /** Thành tiền hạng mục HĐ (định mức) theo lĩnh vực / khu vực. */
    @Builder.Default
    private BigDecimal kpiAmount = BigDecimal.ZERO;
}
