package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;

/** volume_danh_sach_hop_dong: danh sách hợp đồng theo trạng thái ngân sách/quyết toán, có phân trang. */
public record NganHoDanhSachHopDongResponse(PagedResult<NganHoHopDongToolItem> danhSachHopDong) {
}
