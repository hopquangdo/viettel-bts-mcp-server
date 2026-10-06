package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * vuongmac_xep_hang: 1 bảng xếp hạng số vướng mắc đang mở theo đúng 1 chiều ({@code theo}).
 * Chiều trùng cấp bộ lọc hoặc lọc 1 đối tượng cụ thể: không có danhSach, {@code ghiChu} giải thích và gợi ý chiều khác.
 *
 * @param xepHangTheo nhãn chiều, vd "Hợp đồng"
 * @param tieuChi     "Số vướng mắc đang mở, nhiều xuống ít"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VuongMacXepHangResponse(
        String xepHangTheo,
        String tieuChi,
        List<VuongMacXepHangItem> danhSach,
        String ghiChu) {
}
