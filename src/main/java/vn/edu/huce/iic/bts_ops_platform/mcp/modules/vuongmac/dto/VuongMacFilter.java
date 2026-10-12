package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import java.time.LocalDate;

/**
 * Bộ lọc chung của các tool vướng mắc. Đối tượng/hợp đồng/nhà thầu/khu vực/tỉnh/loại HĐ/cán bộ: ID, mã hoặc tên (tự tra cứu).
 * trangThai: pending | in_progress | resolved | rejected. dangMoOnly: chỉ vướng mắc đang mở.
 * fromDate/toDate lọc theo ngày tạo vướng mắc (giờ VN, gồm cả toDate). null = không lọc.
 */
public record VuongMacFilter(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong,
        String trangThai,
        String kieuVuongMac,
        String giaiDoan,
        Boolean dangMoOnly,
        String canBo,
        LocalDate fromDate,
        LocalDate toDate) {
}
