package vn.edu.huce.iic.bts_ops_platform.dto.nhathau;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;

/** Kết quả nhathau_tool — danh sách các nhà thầu đang phụ trách đối tượng (có phân trang). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhaThauQueryResponse {
    /** Tổng số nhà thầu khớp bộ lọc. */
    private long tongSo;
    private PagedResult<NhaThauItem> danhSach;
}
