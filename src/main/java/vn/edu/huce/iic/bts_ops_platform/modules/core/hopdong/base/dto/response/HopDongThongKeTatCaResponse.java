package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.List;

/**
 * Thống kê hợp đồng của TẤT CẢ loại hợp đồng trong 1 lần gọi — thay cho việc gọi
 * {@code /thong-ke?loaiHopDongId=...} lặp lại một lần cho mỗi loại (N+1 phía client).
 */
public record HopDongThongKeTatCaResponse(
        String capNhatLuc,
        /** Số liệu gộp toàn bộ loại (kể cả hợp đồng chưa gán loại) — {@code loaiHopDongId = null}. */
        HopDongThongKeLoaiResponse tongHop,
        List<HopDongThongKeLoaiResponse> theoLoai) {
}
