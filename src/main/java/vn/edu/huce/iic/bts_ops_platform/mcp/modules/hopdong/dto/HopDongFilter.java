package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

/**
 * Bộ lọc chung của các tool hợp đồng: ID, mã hoặc tên (tự tra cứu); null = không lọc.
 * kieuHopDong: kiểu hợp đồng (khác loại hợp đồng = lĩnh vực).
 */
public record HopDongFilter(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong,
        String kieuHopDong) {

    public static HopDongFilter ofHopDong(String hopDong) {
        return new HopDongFilter(null, hopDong, null, null, null, null, null);
    }
}
