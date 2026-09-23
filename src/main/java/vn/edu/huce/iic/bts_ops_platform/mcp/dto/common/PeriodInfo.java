package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

/** Thông tin kỳ đang xem — dùng chung cho mọi tool OVERVIEW có lọc theo khoảng ngày. */
public record PeriodInfo(String fromDate, String toDate, String label) {
}
