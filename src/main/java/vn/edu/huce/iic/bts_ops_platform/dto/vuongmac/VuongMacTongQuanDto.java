package vn.edu.huce.iic.bts_ops_platform.dto.vuongmac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Số liệu tổng hợp vướng mắc/sự cố — dùng cho vuongmac_tongquan. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VuongMacTongQuanDto {
    private long total;
    private long pending;
    private long inProgress;
    private long resolved;
    private long rejected;
    private long overdue30Days;
    /** Số vướng mắc chuyển sang 'resolved' kể từ sinceDate — chỉ có khi truyền sinceDate
     * (xấp xỉ theo ngay_cap_nhat của bản ghi resolved). null nếu không truyền. */
    private Long resolvedTrongKy;
    private Map<String, Long> countsByKieu;
}
