package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import java.util.List;

/**
 * hopdong_thong_ke_theo_loai: mỗi loại hợp đồng 1 dòng, xếp theo số hợp đồng giảm dần.
 *
 * @param nguongChamTienDo "70%": dưới mức này là chậm tiến độ
 */
public record HopDongTheoLoaiResponse(String nguongChamTienDo, List<HopDongTheoLoaiItem> theoLoai) {
}
