package vn.edu.huce.iic.bts_ops_platform.mcp.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ExceptionCode;

/**
 * Mã lỗi dùng chung cho mọi tool AI (common/tools/handler/impls/*) — khi tham số filter
 * (maDoiTuong/maHopDong/nhaThauId/tenNhaThau/khuVuc/...) không khớp bản ghi nào, handler ném
 * AppException(ToolErrorCode.FILTER_NOT_FOUND, message) thay vì âm thầm trả kết quả rỗng.
 * Với tool gọi qua MCP (@Tool trong McpToolDefinitions), Spring AI tự bắt exception và trả
 * message làm kết quả tool cho LLM đọc trực tiếp — không cần try/catch thủ công ở đó.
 */
@Getter
@AllArgsConstructor
public enum ToolErrorCode implements ExceptionCode {
    FILTER_NOT_FOUND(404, "TOOL_FILTER_NOT_FOUND"),
    /** Từ khoá khớp nhiều bản ghi ngang nhau — không đoán, yêu cầu truyền id/mã chính xác. */
    FILTER_AMBIGUOUS(409, "TOOL_FILTER_AMBIGUOUS"),
    /** Giá trị tham số không thuộc tập cho phép (ví dụ tab, trạng thái). */
    FILTER_INVALID(400, "TOOL_FILTER_INVALID");

    private final Integer code;
    private final String type;
}
