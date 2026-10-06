package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** vuongmac_tim_kiem: danh sách vướng mắc theo bộ lọc / từ khóa, có phân trang. */
public record VuongMacTimKiemResponse(PagedResult<VuongMacItemDto> danhSach) {
}
