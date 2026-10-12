package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** vuongmac_qua_han: vướng mắc tạo trước ngưỡng soNgayQuaHan ngày (toàn hệ thống, không theo bộ lọc khác). */
public record VuongMacQuaHanResponse(
        int soNgayQuaHan,
        PagedResult<VuongMacItemDto> quaHan) {
}
