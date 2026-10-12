package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacItemDto;

/** vuongmac_qua_han: vướng mắc tạo trước ngưỡng soNgayQuaHan ngày (toàn hệ thống, không theo bộ lọc khác). */
public record VuongMacQuaHanResponse(
        int soNgayQuaHan,
        PagedResult<VuongMacItemDto> quaHan) {
}
