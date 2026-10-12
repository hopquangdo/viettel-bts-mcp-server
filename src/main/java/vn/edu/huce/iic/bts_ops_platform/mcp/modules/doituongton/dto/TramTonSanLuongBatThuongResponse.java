package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import java.util.List;

/** doituongton_san_luong_bat_thuong: đối tượng có sản lượng bất thường. */
public record TramTonSanLuongBatThuongResponse(List<TramTonToolSanLuongBatThuongItemDto> sanLuongBatThuong) {
}
