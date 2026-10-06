package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import java.util.List;

/**
 * phancong_ty_le_huy_tram: tỷ lệ trạm bị hủy theo nhà thầu/cán bộ được giao hoặc theo khu vực.
 *
 * @param theo             "nha_thau" | "khu_vuc"
 * @param nguong           ngưỡng cảnh báo cấu hình ("15%")
 * @param soNhomVuotNguong số nhóm có tỷ lệ hủy vượt ngưỡng
 * @param danhSach         các nhóm, xếp theo số trạm hủy giảm dần
 */
public record PhanCongTyLeHuyResponse(String theo, String nguong, long soNhomVuotNguong, List<PhanCongTyLeHuyItem> danhSach) {
}
