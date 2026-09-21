package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;

import java.util.List;

/** Kết quả hopdong_tool.theoBuoc — chỉ có khi truyền maHopDong. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongTheoBuocResponse {
    /** Số đối tượng đang ở từng bước của luồng trạng thái hợp đồng — luôn có khi có maHopDong. */
    private List<HopDongBuocPhanBoItem> phanBo;
    /** Lịch sử chuyển bước từ audit_log, phân trang theo page/pageSize — có thể rỗng, xem ghi chú ở HopDongBuocLichSuItem. */
    private PagedResult<HopDongBuocLichSuItem> lichSu;
}
