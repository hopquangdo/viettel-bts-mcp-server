package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.UUID;

/** Số hợp đồng theo từng kiểu hợp đồng — phần {@code soLuongTheoKieu} của thống kê. */
public record HopDongThongKeKieuResponse(
        UUID kieuHopDongId,
        String ma,
        String ten,
        String nhom,
        long soLuong) {
}
