package vn.edu.huce.iic.bts_ops_platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.common.McpToolInfo;

import java.util.List;

/** Debug/introspection cho tool MCP đã đăng ký — KHÔNG phải endpoint giao thức MCP (đó là /mcp). */
@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
@Tag(name = "MCP Tools", description = "Kiểm tra danh sách tool MCP đã nạp — phục vụ debug tích hợp chatbot")
public class McpToolController {

    private final ToolCallbackProvider toolCallbackProvider;

    @GetMapping("/tools")
    @Operation(summary = "Liệt kê toàn bộ tool MCP đã đăng ký (tên, mô tả, input schema)")
    public ResponseEntity<ApiResponse<List<McpToolInfo>>> tools() {
        List<McpToolInfo> result = List.of(toolCallbackProvider.getToolCallbacks()).stream()
                .map(this::toInfo)
                .toList();
        return ApiResponse.<List<McpToolInfo>>build()
                .withData(result)
                .toEntity();
    }

    private McpToolInfo toInfo(ToolCallback callback) {
        var definition = callback.getToolDefinition();
        return new McpToolInfo(definition.name(), definition.description(), definition.inputSchema());
    }
}
