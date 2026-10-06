package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 dòng của hopdong_xep_hang. Cột không thuộc chiều đang xếp để trống: khu vực có soHopDong; tỉnh có soHoanThanh và
 * tyLeHoanThanh ("82,5%").
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HopDongXepHangItem(
        int hang,
        String ten,
        Long soHopDong,
        long soDoiTuong,
        Long soHoanThanh,
        String tyLeHoanThanh,
        long soVuongMac) {
}
