package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * vuongmac_tong_quan: số vướng mắc theo trạng thái và loại.
 *
 * @param dangVuong      vướng mắc chưa giải quyết = choXuLy + dangXuLy (ô "Đang vướng" trên Dashboard)
 * @param quaHan30Ngay   số vướng mắc đang mở tồn quá 30 ngày kể từ khi tạo
 * @param daXuLyTrongKy  số vướng mắc chuyển sang đã xử lý trong kỳ, chỉ có khi truyền fromDate hoặc sinceDate
 * @param theoLoai       số vướng mắc theo loại (nhãn tiếng Việt)
 * @param soHopDongCoVuongMacMo  số hợp đồng khác nhau đang có ít nhất 1 vướng mắc mở
 * @param soDoiTuongCoVuongMacMo số đối tượng khác nhau đang có ít nhất 1 vướng mắc mở
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VuongMacTongQuanToolResponse(
        long tongSo,
        long choXuLy,
        long dangXuLy,
        long daXuLy,
        long tuChoi,
        long dangVuong,
        long quaHan30Ngay,
        Long daXuLyTrongKy,
        Map<String, Long> theoLoai,
        long soHopDongCoVuongMacMo,
        long soDoiTuongCoVuongMacMo) {
}
