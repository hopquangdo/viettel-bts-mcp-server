package vn.edu.huce.iic.bts_ops_platform.mcp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.WebMvcStatelessServerTransport;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Replaces Spring AI's auto-configured stateless HTTP transport (it backs off when a bean of this
 * type exists) only to add a {@code contextExtractor}: the auto-configuration offers no other way to
 * see the HTTP headers of an MCP request. Every other setting mirrors the auto-configuration.
 */
@Configuration
public class McpTransportConfig {

    @Bean
    public WebMvcStatelessServerTransport webMvcStatelessServerTransport(
            @Qualifier("mcpServerObjectMapper") ObjectMapper objectMapper,
            McpServerStreamableHttpProperties properties,
            McpUserContext userContext) {
        return WebMvcStatelessServerTransport.builder()
                .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
                .messageEndpoint(properties.getMcpEndpoint())
                .contextExtractor(request -> userContext.fromHeaders(request.headers()::firstHeader))
                .build();
    }
}
