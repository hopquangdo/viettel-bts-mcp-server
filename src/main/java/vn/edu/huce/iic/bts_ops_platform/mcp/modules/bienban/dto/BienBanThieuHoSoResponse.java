package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** bienban_thieu_ho_so: hợp đồng còn thiếu biên bản/hồ sơ bắt buộc theo checklist, có phân trang. */
public record BienBanThieuHoSoResponse(PagedResult<BienBanThieuTheoTienDoToolItem> thieuHoSo) {
}
