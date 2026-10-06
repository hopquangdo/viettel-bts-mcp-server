package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response;

import java.math.BigDecimal;

/** Object-weighted completion summary for the selected SanLuong scope. */
public record SanLuongProgressSummaryResponse(
        BigDecimal tyLeHoanThanh,
        long soDoiTuongHoanThanh,
        long soDoiTuongDangThiCong,
        long soDoiTuongChuaBatDau,
        long soDoiTuongHoanThanhThap) {
}