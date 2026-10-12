package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto;

import java.time.LocalDate;

/**
 * Bộ lọc chung của các tool biên bản. Khu vực/tỉnh/hợp đồng/nhà thầu/đối tượng: ID, mã hoặc tên (tự tra cứu).
 * fromDate/toDate: khoảng ngày (tự đảo nếu nhập ngược). null = không lọc.
 */
public record BienBanFilter(
        String khuVuc,
        String tinhThanh,
        String hopDong,
        String nhaThau,
        String doiTuong,
        LocalDate fromDate,
        LocalDate toDate) {
}
