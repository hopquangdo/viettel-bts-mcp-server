package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import java.util.List;

/** doituongton_thieu_dieu_kien: đối tượng thiếu từ 2 điều kiện quyết toán trở lên. */
public record TramTonThieuDieuKienResponse(List<TramTonThieuNhieuDieuKienToolItem> thieuNhieuDieuKien) {
}
