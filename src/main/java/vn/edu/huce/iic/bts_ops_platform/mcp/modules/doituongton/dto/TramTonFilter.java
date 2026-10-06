package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

/**
 * Bộ lọc chung của các tool doituongton_*. Đối tượng/hợp đồng/nhà thầu/khu vực/tỉnh/loại HĐ: ID, mã hoặc tên (tự tra cứu).
 * null = không lọc.
 */
public record TramTonFilter(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong) {
}
