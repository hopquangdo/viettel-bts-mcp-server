package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VuongMacTongQuanResponse {
    private long total;
    private long pending;
    private long inProgress;
    private long resolved;
    private long rejected;
    private long overdue30Days;
    /** Đếm theo kiểu vướng mắc — key là slug (giai_phong_mat_bang, thiet_ke, ...). */
    private java.util.Map<String, Long> countsByKieu;
}
