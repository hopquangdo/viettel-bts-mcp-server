package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.Map;
import java.util.UUID;

/** Một dòng trong {@code hopDongChiTiet} — tiến độ đối tượng của 1 hợp đồng. */
public record HopDongThongKeHopDongResponse(
        UUID hopDongId,
        String maHopDong,
        String ten,
        long soDoiTuong,
        Map<String, Long> tienDoTheoTrangThai,
        double tyLeHoanThanh,
        long soVuongMac) {
}
