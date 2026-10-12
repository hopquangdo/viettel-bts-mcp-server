package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import java.util.List;

/** doituongton_thieu_cap_nhat: đối tượng không cập nhật thi công quá soNgayThieuCapNhat ngày. */
public record TramTonThieuCapNhatResponse(int soNgayThieuCapNhat, List<TramTonToolThieuCapNhatItemDto> thieuCapNhat) {
}
