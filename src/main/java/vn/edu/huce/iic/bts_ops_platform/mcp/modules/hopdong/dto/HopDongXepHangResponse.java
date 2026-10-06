package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * hopdong_xep_hang: 1 bảng xếp hạng theo đúng 1 chiều ({@code theo}). Chiều trùng cấp bộ lọc, chiều tỉnh khi chưa lọc
 * 1 hợp đồng, hoặc lọc 1 đối tượng cụ thể: không có danhSach, {@code ghiChu} giải thích và gợi ý chiều khác.
 *
 * @param xepHangTheo nhãn chiều, vd "Khu vực"
 * @param tieuChi     vd "Số hợp đồng, nhiều xuống ít"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HopDongXepHangResponse(
        String xepHangTheo,
        String tieuChi,
        List<HopDongXepHangItem> danhSach,
        String ghiChu) {
}
