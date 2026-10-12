package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** phancong_danh_sach: các bản ghi phân công theo bộ lọc, có phân trang. */
public record PhanCongDanhSachResponse(PagedResult<PhanCongToolItem> danhSach) {
}
