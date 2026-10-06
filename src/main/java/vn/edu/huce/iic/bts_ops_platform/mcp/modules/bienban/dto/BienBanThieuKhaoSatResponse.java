package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** bienban_thieu_khao_sat: trạm chưa có biên bản khảo sát đã duyệt, có phân trang. */
public record BienBanThieuKhaoSatResponse(PagedResult<BienBanThieuKhaoSatToolItem> thieuKhaoSat) {
}
