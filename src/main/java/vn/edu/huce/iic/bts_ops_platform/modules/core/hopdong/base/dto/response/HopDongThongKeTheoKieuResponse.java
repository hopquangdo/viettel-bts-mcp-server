package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Khối thống kê của 1 kiểu hợp đồng trong 1 loại — cùng tập số liệu mà
 * {@code GET /api/v1/hop-dong/thong-ke?loaiHopDongId=...&kieuHopDongId=...} trả về cho cặp đó,
 * gồm cả {@code tienDoTheoLuongThucTe} (luồng trạng thái cấu hình riêng cho kiểu này).
 */
public record HopDongThongKeTheoKieuResponse(
        UUID kieuHopDongId,
        String ma,
        String ten,
        String nhom,
        long tongHopDong,
        long tongHoatDong,
        long tongDoiTuong,
        Map<String, Long> tienDoTheoTrangThai,
        double tyLeHoanThanh,
        /** Rỗng khi kiểu này chưa được gán luồng trạng thái. */
        List<HopDongDanhSachTienDoBuocResponse> tienDoTheoLuongThucTe,
        List<HopDongThongKeHopDongResponse> hopDongChiTiet,
        long soHopDongChamTienDo,
        long soHopDongVuongMac) {
}
