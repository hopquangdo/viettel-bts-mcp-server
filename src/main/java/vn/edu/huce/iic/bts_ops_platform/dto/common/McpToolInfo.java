package vn.edu.huce.iic.bts_ops_platform.dto.common;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * 1 tool MCP đã đăng ký — trả về cho GET /api/v1/mcp/tools để debug/kiểm tra nạp tool.
 * inputSchema giữ nguyên chuỗi JSON thô (đã hợp lệ từ Spring AI) — @JsonRawValue để Jackson xuất
 * thẳng thành object JSON thay vì escape thành string, tránh phụ thuộc ObjectMapper tự readTree.
 */
public record McpToolInfo(String name, String description, @JsonRawValue String inputSchema) {
}
