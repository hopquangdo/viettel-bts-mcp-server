package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** phancong_chua_phan_cong: đối tượng chưa giao nhà thầu. */
public record PhanCongChuaPhanCongResponse(PagedResult<PhanCongChuaPhanCongToolItem> chuaPhanCong) {
}
