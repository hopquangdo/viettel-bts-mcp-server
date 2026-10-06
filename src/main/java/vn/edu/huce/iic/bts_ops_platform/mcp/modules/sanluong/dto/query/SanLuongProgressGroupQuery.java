package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query;

/** Aggregated objects sharing the same total and completed work-item counts. */
public record SanLuongProgressGroupQuery(
        Long soDoiTuong,
        Integer tongHangMuc,
        Integer hangMucHoanThanh) {
}