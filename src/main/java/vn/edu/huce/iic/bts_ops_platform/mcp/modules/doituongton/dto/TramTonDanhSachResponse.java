package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** doituongton_danh_sach: danh sách đối tượng tồn theo nhóm (tab), có phân trang. */
public record TramTonDanhSachResponse(PagedResult<TramTonToolItemDto> danhSach) {
}
