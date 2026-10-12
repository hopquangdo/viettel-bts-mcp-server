package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import java.time.LocalDate;

/**
 * Bộ lọc chung của các tool sản lượng: ID, mã hoặc tên (tự tra cứu); null = không lọc.
 * doiTuong nhận 1 hoặc nhiều giá trị ngăn cách bằng dấu phẩy/chấm phẩy. Không truyền ngày = toàn bộ thời gian.
 */
public record SanLuongFilter(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong,
        LocalDate fromDate,
        LocalDate toDate) {

    public static SanLuongFilter ofDoiTuong(String doiTuong) {
        return new SanLuongFilter(doiTuong, null, null, null, null, null, null, null);
    }
}
