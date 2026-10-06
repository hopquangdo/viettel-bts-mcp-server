package vn.edu.huce.iic.bts_ops_platform.mcp.common.loc;

/**
 * Bộ lọc phạm vi thô dùng chung. Các giá trị là ID, mã hoặc tên để tầng xử lý tự tra cứu;
 * null nghĩa là không lọc theo chiều đó.
 */
public record PhamViLoc(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong) {
}