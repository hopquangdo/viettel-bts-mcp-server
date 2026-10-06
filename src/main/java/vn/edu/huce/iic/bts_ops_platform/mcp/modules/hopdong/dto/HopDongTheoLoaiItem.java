package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.Tien;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 1 dòng của hopdong_thong_ke_theo_loai: chỉ số hopdong_tong_quan khi lọc đúng 1 loại hợp đồng.
 *
 * @param tyLeHoanThanh       "82,5%"
 * @param tienDoTheoTrangThai số hợp đồng theo trạng thái thi công (nhãn tiếng Việt)
 * @param soDoiTuongHoatDong  thẻ lĩnh vực trên Dashboard: số đối tượng đang hoạt động của loại (khác tongDoiTuong của màn Tiến độ)
 * @param soDoiTuongTheoKieu  thẻ lĩnh vực trên Dashboard: số đối tượng theo loại đối tượng ("Tuyến": 858, "Trạm": 39...)
 * @param tongSanLuong        thẻ lĩnh vực trên Dashboard: tổng thành tiền sản lượng hoàn thành của loại, mọi thời điểm
 * @param soVuongMacDangVuong thẻ lĩnh vực trên Dashboard: số vướng mắc chờ/đang xử lý của loại
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HopDongTheoLoaiItem(
        String loaiHopDong,
        long soHopDong,
        long tongDoiTuong,
        String tyLeHoanThanh,
        long soHopDongChamTienDo,
        long soHopDongVuongMac,
        Map<String, Long> tienDoTheoTrangThai,
        Long soDoiTuongHoatDong,
        Map<String, Long> soDoiTuongTheoKieu,
        @Tien BigDecimal tongSanLuong,
        Long soVuongMacDangVuong) {
}
