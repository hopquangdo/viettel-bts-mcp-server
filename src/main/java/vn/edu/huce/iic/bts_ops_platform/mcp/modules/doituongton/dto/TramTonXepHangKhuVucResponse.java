package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * doituongton_xep_hang_khu_vuc: top khu vực theo số đối tượng tồn chờ quyết toán.
 * Lọc khuVuc hoặc 1 đối tượng cụ thể thì không có bảng xếp hạng (xepHangKhuVuc = null).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TramTonXepHangKhuVucResponse(List<TramTonXepHangKhuVucToolItem> xepHangKhuVuc) {
}
