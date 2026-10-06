package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * hopdong_tong_quan: chỉ số tổng hợp hợp đồng, đã định dạng cho LLM đọc.
 *
 * @param tyLeHoanThanh            "82,5%"
 * @param tienDoTheoTrangThai      số hợp đồng theo trạng thái thi công (nhãn tiếng Việt)
 * @param soHopDongChamTienDo      số hợp đồng có tỷ lệ hoàn thành dưới {@code nguongChamTienDo}
 * @param soHopDongVuongMac        ô "Có vướng mắc" trên màn Tiến độ hợp đồng: số vướng mắc đang mở của các hợp đồng có đối tượng
 * @param doiTuongHoanThanhTrongKy số đối tượng hoàn thành trong [fromDate, toDate], chỉ có khi truyền ngày
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HopDongTongQuanToolResponse(
        long tongHopDong,
        long tongHopDongHoatDong,
        long tongDoiTuong,
        String tyLeHoanThanh,
        Map<String, Long> tienDoTheoTrangThai,
        List<TheoKieu> soLuongTheoKieu,
        Map<String, Long> soLuongTheoLoai,
        long soHopDongChamTienDo,
        String nguongChamTienDo,
        long soHopDongVuongMac,
        Long doiTuongHoanThanhTrongKy) {

    /** Số hợp đồng theo 1 kiểu hợp đồng. */
    public record TheoKieu(String ten, String nhom, long soLuong) {
    }
}
