package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** nguonviec_danh_sach: 1 trang hợp đồng nguồn việc theo bộ lọc và tab. */
public record NguonViecDanhSachResponse(
        PagedResult<NguonViecRowToolItem> danhSach) {
}
