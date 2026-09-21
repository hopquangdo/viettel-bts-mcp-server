package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import vn.edu.huce.iic.bts_ops_platform.security.UserScopedToolCallback;
import org.springframework.context.annotation.Configuration;
import vn.edu.huce.iic.bts_ops_platform.definitions.McpToolDefinitions;

/** common/tools: toàn bộ MCP tool nghiệp vụ khai báo trong 1 class {@link McpToolDefinitions}. */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider mcpTools(McpToolDefinitions mcpToolDefinitions) {
        ToolCallback[] rawCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(mcpToolDefinitions)
                .build()
                .getToolCallbacks();
        ToolCallback[] loggedCallbacks = new ToolCallback[rawCallbacks.length];
        for (int i = 0; i < rawCallbacks.length; i++) {
            // Logging outermost so denied calls are logged too; UserScoped runs the tool as the end user.
            loggedCallbacks[i] = new ToolCallLoggingDecorator(new UserScopedToolCallback(rawCallbacks[i]));
        }
        return ToolCallbackProvider.from(loggedCallbacks);
    }
}
