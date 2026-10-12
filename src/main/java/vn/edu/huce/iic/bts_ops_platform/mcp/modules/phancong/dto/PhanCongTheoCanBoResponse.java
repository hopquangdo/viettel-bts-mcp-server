package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.CanBoInfo;

/** phancong_theo_can_bo: phân công đang hoạt động của 1 cán bộ / tài khoản nhà thầu. */
public record PhanCongTheoCanBoResponse(
        CanBoInfo canBo,
        PagedResult<PhanCongToolItem> phanCong) {
}
