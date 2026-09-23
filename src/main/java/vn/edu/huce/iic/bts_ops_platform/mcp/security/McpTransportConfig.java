package vn.edu.huce.iic.bts_ops_platform.mcp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * Replaces Spring AI's auto-configured Streamable HTTP transport (it backs off when a bean of this
 * type exists) only to add a {@code contextExtractor}: the auto-configuration offers no other way to
 * see the HTTP headers of an MCP request. Every other setting mirrors the auto-configuration.
 */
@Configuration
public class McpTransportConfig {

    @Bean
    public WebMvcStreamableServerTransportProvider webMvcStreamableServerTransportProvider(
            ObjectMapper objectMapper,
            McpServerStreamableHttpProperties properties,
            McpUserContext userContext) {
        return WebMvcStreamableServerTransportProvider.builder()
                .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
                .mcpEndpoint(properties.getMcpEndpoint())
                .keepAliveInterval(properties.getKeepAliveInterval())
                .disallowDelete(properties.isDisallowDelete())
                .contextExtractor(request ->
                        userContext.fromAuthorizationHeader(request.headers().firstHeader(HttpHeaders.AUTHORIZATION)))
                .build();
    }
}
