package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** bienban_theo_trang_thai: danh sách biên bản theo trạng thái duyệt, có phân trang. */
public record BienBanTheoTrangThaiResponse(PagedResult<BienBanTheoTrangThaiToolItem> theoTrangThai) {
}
