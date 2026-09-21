package vn.edu.huce.iic.bts_ops_platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Bọc quanh 1 ToolCallback bất kỳ (không riêng MCP) để log tên tool, tham số đầu vào (JSON), kết
 * quả, và thời gian thực thi — cấu hình 1 lần ở McpToolConfig, tự áp dụng cho mọi tool đăng ký,
 * không cần sửa từng lớp Tools khi thêm tool mới.
 */
public class ToolCallLoggingDecorator implements ToolCallback {

    private static final Logger log = LoggerFactory.getLogger("tool.call");

    private final ToolCallback delegate;

    public ToolCallLoggingDecorator(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        return callWithLogging(toolInput, () -> delegate.call(toolInput));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return callWithLogging(toolInput, () -> delegate.call(toolInput, toolContext));
    }

    private String callWithLogging(String toolInput, java.util.function.Supplier<String> invocation) {
        String name = delegate.getToolDefinition().name();
        long start = System.currentTimeMillis();
        log.info("tool call: {} input={}", name, toolInput);
        try {
            String result = invocation.get();
            log.info("tool result: {} durationMs={} output={}", name, System.currentTimeMillis() - start, result);
            return result;
        } catch (RuntimeException ex) {
            log.warn("tool error: {} durationMs={} error={}", name, System.currentTimeMillis() - start, ex.toString());
            throw ex;
        }
    }
}
