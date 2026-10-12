package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto;

/**
 * Bộ lọc chung của các tool ngân hộ (volume_*). hopDong/loaiHopDong/khuVuc/tinhThanh: ID, mã hoặc tên (tự tra cứu).
 * query: từ khóa mã/tên hợp đồng. heSo: hệ số ngưỡng GCCC (ghi đè cấu hình). nguongCanhBao: ngưỡng % cảnh báo (mặc định 90).
 * null = không lọc / dùng mặc định.
 */
public record NganHoFilter(
        String hopDong,
        String query,
        Double heSo,
        Double nguongCanhBao,
        String loaiHopDong,
        String khuVuc,
        String tinhThanh) {
}
