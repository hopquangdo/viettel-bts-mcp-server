package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * nguonviec_tong_quan: tổng giá trị nguồn việc và số hợp đồng theo nhóm, sau khi áp bộ lọc; tiền đã định dạng ("1,25 tỷ đồng").
 *
 * @param sanLuongChuaCoDoanhThu  phần sản lượng đã thực hiện nhưng chưa ghi nhận doanh thu
 * @param giaTriConLai            giá trị hợp đồng còn lại chưa thực hiện
 * @param chuaKhaThi              phần giá trị còn lại chưa khả thi
 * @param soHopDongDangTrienKhai  đang triển khai hoặc vướng pháp lý
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguonViecTongQuanResponse(
        String kyBaoCao,
        String giaTriHopDong,
        String sanLuongThucHien,
        String doanhThu,
        String sanLuongChuaCoDoanhThu,
        String giaTriConLai,
        String chuaKhaThi,
        long tongSoHopDong,
        long soHopDongDangTrienKhai,
        long soHopDongGanHoanThanh) {
}
