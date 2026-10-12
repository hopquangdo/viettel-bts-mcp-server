package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import java.time.LocalDate;

/**
 * Bộ lọc chung của các tool phân công. Nhà thầu/hợp đồng/khu vực/tỉnh/đối tượng: ID, mã hoặc tên (tự tra cứu).
 * vung: mã vùng quản lý. giaiDoan: khớp đúng, không phân biệt hoa thường.
 * fromDate/toDate lọc theo ngày tạo phân công (giờ VN, gồm cả toDate). null = không lọc.
 */
public record PhanCongFilter(
        String vung,
        String nhaThau,
        String hopDong,
        String khuVuc,
        String tinhThanh,
        String doiTuong,
        String giaiDoan,
        LocalDate fromDate,
        LocalDate toDate) {
}
