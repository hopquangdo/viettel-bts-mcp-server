package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * sanluong_tong_quan: chỉ số tổng hợp sản lượng trong kỳ, đã định dạng cho LLM đọc (tiền "1,25 tỷ đồng", "82,5%").
 *
 * @param kyBaoCao                  "01/01/2026 – 30/09/2026"
 * @param tongSoDoiTuong            tổng đối tượng trong kỳ, gồm cả đối tượng chưa có sản lượng (= có + chưa có)
 * @param soDoiTuongCoSanLuong      số đối tượng đã có sản lượng trong kỳ
 * @param giaTriTrongKy             tổng giá trị sản lượng đã báo trong kỳ
 * @param tyLeHoanThanhHangMuc      tỷ lệ hoàn thành hạng mục bình quân theo đối tượng
 * @param soDoiTuongHoanThanhThap   số đối tượng đang thi công có tỷ lệ hoàn thành dưới {@code nguongHoanThanhThap}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongTongQuanToolResponse(
        String kyBaoCao,
        int tongSoDoiTuong,
        int soDoiTuongCoSanLuong,
        int soDoiTuongChuaCoSanLuong,
        String giaTriTrongKy,
        String giaTriHomNay,
        String giaTriTrungBinhMoiDoiTuong,
        int soVuongMacDangMo,
        String tyLeHoanThanhHangMuc,
        int soDoiTuongHoanThanh,
        int soDoiTuongDangThiCong,
        int soDoiTuongChuaBatDau,
        int soDoiTuongHoanThanhThap,
        String nguongHoanThanhThap,
        SoVoiKyTruoc soVoiKyTruoc) {

    /**
     * @param kyTruoc        "Tháng trước", "Cùng kỳ năm trước"...
     * @param tangTruong     "+12,5%" / "-3%"
     * @param xuHuong        "Tăng" | "Giảm" | "Ổn định" | "Chưa có dữ liệu kỳ trước"
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SoVoiKyTruoc(String kyTruoc, String giaTriKyTruoc, String tangTruong, String xuHuong) {
    }
}
