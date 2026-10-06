package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** Kết quả phân trang cho hopdong_search. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongTimKiemResponse {
    private PagedResult<HopDongTimKiemItem> danhSach;
}
