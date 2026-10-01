package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;

import java.util.List;

/** common/tools: gom MCP tool nghiệp vụ từ mọi bean {@link McpToolGroup} (mỗi module 1 class định nghĩa tool). */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider mcpTools(List<McpToolGroup> toolGroups) {
        ToolCallback[] rawCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(toolGroups.toArray())
                .build()
                .getToolCallbacks();
        ToolCallback[] loggedCallbacks = new ToolCallback[rawCallbacks.length];
        for (int i = 0; i < rawCallbacks.length; i++) {
            loggedCallbacks[i] = new ToolCallLoggingDecorator(rawCallbacks[i]);
        }
        return ToolCallbackProvider.from(loggedCallbacks);
    }
}
