package vn.edu.huce.iic.bts_ops_platform.mcp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.metrics.McpSqlCounter;
import vn.edu.huce.iic.bts_ops_platform.mcp.metrics.McpToolStats;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
@Tag(name = "Tool Test", description = "Gọi trực tiếp tool MCP đã đăng ký bằng tên và tham số JSON")
public class ToolTestController {

    private final ToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<McpToolStats> toolStats;

    @GetMapping("/{toolName}")
    @Operation(summary = "Gọi tool MCP đã đăng ký bằng query parameters")
    public ResponseEntity<ApiResponse<JsonNode>> getTool(
            @PathVariable String toolName,
            @RequestParam MultiValueMap<String, String> parameters) {
        ObjectNode arguments = objectMapper.createObjectNode();
        parameters.forEach((name, values) -> {
            if (values.size() == 1) {
                arguments.put(name, values.get(0));
            } else {
                ArrayNode array = arguments.putArray(name);
                values.forEach(array::add);
            }
        });
        return invoke(toolName, arguments);
    }

    @PostMapping("/{toolName}")
    @Operation(summary = "Gọi tool MCP đã đăng ký bằng object JSON")
    public ResponseEntity<ApiResponse<JsonNode>> postTool(
            @PathVariable String toolName,
            @RequestBody JsonNode arguments) {
        if (arguments == null || !arguments.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tool arguments must be a JSON object");
        }
        return invoke(toolName, arguments);
    }

    @GetMapping("/thong-ke-hieu-nang")
    @Operation(summary = "Báo cáo hiệu năng tool MCP: số lần gọi, thời gian min/TB/p50/p95/max và số câu SQL mỗi lần gọi (từ lúc khởi động hoặc lần reset)")
    public ResponseEntity<ApiResponse<McpToolStats.Report>> thongKeHieuNang(
            @RequestParam(required = false, defaultValue = "false") boolean reset) {
        McpToolStats stats = toolStats.getIfAvailable();
        McpToolStats.Report report = stats != null ? stats.report(reset)
                : new McpToolStats.Report(java.time.Instant.now(), java.time.Instant.now(), List.of());
        return ApiResponse.<McpToolStats.Report>build().withData(report).toEntity();
    }

    private ResponseEntity<ApiResponse<JsonNode>> invoke(String toolName, JsonNode arguments) {
        ToolCallback callback = findCallback(toolName);
        String toolInput = serializeArguments(arguments);
        long start = System.nanoTime();
        boolean outermost = McpSqlCounter.begin();
        boolean ok = false;
        try {
            String result = callback.call(toolInput);
            JsonNode data = parseResult(result);
            ok = true;
            return ApiResponse.<JsonNode>build().withData(data).toEntity();
        } finally {
            if (outermost) {
                int sql = McpSqlCounter.end();
                McpToolStats stats = toolStats.getIfAvailable();
                if (stats != null) {
                    stats.record(callback.getToolDefinition().name(), System.nanoTime() - start, sql, ok);
                }
            }
        }
    }

    private ToolCallback findCallback(String toolName) {
        return List.of(toolCallbackProvider.getToolCallbacks()).stream()
                .filter(callback -> callback.getToolDefinition().name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tool not found: " + toolName));
    }

    private String serializeArguments(JsonNode arguments) {
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Tool arguments could not be serialized", ex);
        }
    }

    private JsonNode parseResult(String result) {
        if (result == null) {
            return null;
        }
        try {
            JsonNode parsed = objectMapper.readTree(result);
            return parsed != null ? parsed : objectMapper.getNodeFactory().textNode(result);
        } catch (JsonProcessingException ex) {
            return objectMapper.getNodeFactory().textNode(result);
        }
    }
}
