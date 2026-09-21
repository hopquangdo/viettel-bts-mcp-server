package vn.edu.huce.iic.bts_ops_platform.security;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtService;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;

import java.util.Map;
import java.util.Optional;

/**
 * Identifies the end user behind an MCP call.
 *
 * <p>The chatbot forwards the short-lived token the backend issued for the current chat turn
 * ({@code Authorization: Bearer}, {@code aud=mcp}). It is verified when the HTTP request is turned
 * into an {@link McpTransportContext}; tool code later reads the verified principal from the
 * {@link ToolContext}. The request thread is not the thread that runs the tool, so the
 * {@code SecurityContext} ThreadLocal cannot be relied upon between the two.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpUserContext {

    static final String PRINCIPAL_KEY = "mcp.user";
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final PhanQuyenResolverService phanQuyenResolverService;

    /** Verifies the Authorization header. A missing or invalid token yields a context with no user. */
    public McpTransportContext fromAuthorizationHeader(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER)) {
            return McpTransportContext.EMPTY;
        }
        try {
            JwtUserPrincipal principal = jwtService.verifyMcpToken(authorization.substring(BEARER.length()).trim());
            JwtUserPrincipal enriched = principal.withAuthorities(
                    phanQuyenResolverService.resolveAuthorities(principal.quyenId()));
            return McpTransportContext.create(Map.of(PRINCIPAL_KEY, enriched));
        } catch (AppException e) {
            log.warn("MCP request carries an invalid user token: {}", e.getMessage());
            return McpTransportContext.EMPTY;
        }
    }

    /** The user of the running tool call, if the request carried a valid token. */
    public static Optional<JwtUserPrincipal> currentUser(ToolContext toolContext) {
        if (toolContext == null) {
            return Optional.empty();
        }
        return McpToolUtils.getMcpExchange(toolContext)
                .map(McpSyncServerExchange::transportContext)
                .map(context -> context.get(PRINCIPAL_KEY))
                .filter(JwtUserPrincipal.class::isInstance)
                .map(JwtUserPrincipal.class::cast);
    }
}
