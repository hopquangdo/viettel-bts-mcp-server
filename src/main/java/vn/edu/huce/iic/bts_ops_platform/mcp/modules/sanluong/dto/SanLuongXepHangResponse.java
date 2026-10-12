package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * sanluong_xep_hang: 1 bảng xếp hạng theo đúng 1 chiều ({@code theo}). Giá trị đã định dạng tiếng Việt cho LLM đọc.
 * Chiều trùng cấp bộ lọc hoặc lọc 1 đối tượng cụ thể: không có danhSach, {@code ghiChu} giải thích và gợi ý chiều khác.
 *
 * @param kyBaoCao          "01/01/2026 – 30/09/2026"
 * @param xepHangTheo       nhãn chiều, vd "Khu vực"
 * @param tieuChi           vd "Giá trị sản lượng, cao xuống thấp"
 * @param tongSo            tổng số thực thể của chiều (trước khi cắt top)
 * @param caoNhatGiaTri     thực thể có giá trị sản lượng cao nhất trong danhSach
 * @param caoNhatHoanThanh  thực thể có tỷ lệ hoàn thành cao nhất trong danhSach
 * @param thapNhatHoanThanh thực thể có tỷ lệ hoàn thành thấp nhất trong danhSach
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongXepHangResponse(
        String kyBaoCao,
        String xepHangTheo,
        String tieuChi,
        Integer tongSo,
        SanLuongXepHangItem caoNhatGiaTri,
        SanLuongXepHangItem caoNhatHoanThanh,
        SanLuongXepHangItem thapNhatHoanThanh,
        List<SanLuongXepHangItem> danhSach,
        String ghiChu) {
}
