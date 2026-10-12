package vn.edu.huce.iic.bts_ops_platform.mcp.exception;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;

/**
 * Tham số tool sai giá trị (thiếu tham số bắt buộc, mã ngoài tập cho phép...). Là {@link AppException} mã
 * {@link ToolErrorCode#FILTER_INVALID} nên đi đúng đường báo lỗi sẵn có: Spring AI trả message cho LLM đọc để tự sửa tham số.
 * Message viết tiếng Việt, luôn kèm danh sách giá trị hợp lệ.
 */
public class McpInvalidParamException extends AppException {

    public McpInvalidParamException(String message) {
        super(ToolErrorCode.FILTER_INVALID, message);
    }
}
