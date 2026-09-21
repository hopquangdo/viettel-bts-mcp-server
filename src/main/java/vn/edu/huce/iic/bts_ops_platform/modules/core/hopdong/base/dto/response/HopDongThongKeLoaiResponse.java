package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Khối thống kê của 1 loại hợp đồng — cùng tập số liệu mà
 * {@code GET /api/v1/hop-dong/thong-ke?loaiHopDongId=...} trả về cho riêng loại đó.
 * Dòng tổng (tất cả loại) dùng lại chính record này với {@code loaiHopDongId = null}.
 */
public record HopDongThongKeLoaiResponse(
        UUID loaiHopDongId,
        String ma,
        String ten,
        long tongHopDong,
        long tongHoatDong,
        long tongDoiTuong,
        Map<String, Long> tienDoTheoTrangThai,
        double tyLeHoanThanh,
        List<HopDongThongKeKieuResponse> soLuongTheoKieu,
        /**
         * Thống kê đầy đủ của từng kiểu thuộc loại này — thay cho việc gọi
         * {@code /thong-ke?loaiHopDongId=...&kieuHopDongId=...} một lần mỗi kiểu.
         * Luôn liệt kê MỌI kiểu trong danh mục của loại, kể cả kiểu chưa có hợp đồng nào
         * (các số về 0) để giao diện dựng đủ tab. Rỗng ở dòng {@code tongHop}.
         */
        List<HopDongThongKeTheoKieuResponse> theoKieu,
        List<HopDongThongKeHopDongResponse> hopDongChiTiet,
        long soHopDongChamTienDo,
        long soHopDongVuongMac) {
}
