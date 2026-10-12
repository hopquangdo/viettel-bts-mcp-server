package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

/**
 * 1 dòng xếp hạng hợp đồng (nội bộ, chung cho mọi chiều). Cột không thuộc chiều đó để null:
 * khu vực có soHopDong, tỉnh có soHoanThanh.
 */
public record HopDongXepHangDong(String ten, Long soHopDong, long soDoiTuong, Long soHoanThanh, long soVuongMac) {
}
