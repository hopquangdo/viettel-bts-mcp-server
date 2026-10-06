package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacItemDto;

/** vuongmac_tim_kiem: danh sách vướng mắc theo bộ lọc / từ khóa, có phân trang. */
public record VuongMacTimKiemResponse(PagedResult<VuongMacItemDto> danhSach) {
}
