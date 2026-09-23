package vn.edu.huce.iic.bts_ops_platform.mcp.security;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.McpUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.PhanQuyenResolverService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Identifies the end user behind an MCP call.
 *
 * <p>Identity is trusted from headers set by the caller (ai-assistant/backend), which has already
 * authenticated the user — the MCP server itself only checks {@code X-API-Key} (see
 * {@code McpApiKeyFilter}) and reads {@code quyenId}/{@code khuVucId} straight off the request. The
 * request thread is not the thread that runs the tool, so the {@code SecurityContext} ThreadLocal
 * cannot be relied upon between the two; the resolved principal is carried via the
 * {@link McpTransportContext} instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpUserContext {

    static final String PRINCIPAL_KEY = "mcp.user";

    static final String HEADER_USER_ID = "X-User-Id";
    static final String HEADER_TEN_DANG_NHAP = "X-User-Ten-Dang-Nhap";
    static final String HEADER_HO_TEN = "X-User-Ho-Ten";
    static final String HEADER_QUYEN_ID = "X-User-Quyen-Id";
    static final String HEADER_KHU_VUC_ID = "X-User-Khu-Vuc-Id";

    private final PhanQuyenResolverService phanQuyenResolverService;

    /** Builds the transport context for an MCP request from its identity headers. */
    public McpTransportContext fromHeaders(Function<String, String> header) {
        return resolve(header)
                .<McpTransportContext>map(user -> McpTransportContext.create(Map.of(PRINCIPAL_KEY, user)))
                .orElse(McpTransportContext.EMPTY);
    }

    /** Resolves the identity headers of a servlet request, e.g. for {@code McpApiKeyFilter}. */
    public Optional<McpUserPrincipal> resolve(HttpServletRequest request) {
        return resolve(request::getHeader);
    }

    private Optional<McpUserPrincipal> resolve(Function<String, String> header) {
        String id = header.apply(HEADER_USER_ID);
        if (!StringUtils.hasText(id)) {
            return Optional.empty();
        }
        try {
            McpUserPrincipal principal = new McpUserPrincipal(
                    UUID.fromString(id.trim()),
                    header.apply(HEADER_TEN_DANG_NHAP),
                    header.apply(HEADER_HO_TEN),
                    parseUuid(header.apply(HEADER_QUYEN_ID)),
                    parseUuid(header.apply(HEADER_KHU_VUC_ID)));
            return Optional.of(principal.withAuthorities(
                    phanQuyenResolverService.resolveAuthorities(principal.quyenId())));
        } catch (IllegalArgumentException ex) {
            log.warn("MCP request carries invalid identity headers: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static UUID parseUuid(String value) {
        return StringUtils.hasText(value) ? UUID.fromString(value.trim()) : null;
    }

    /** The user of the running tool call, if the request carried valid identity headers. */
    public static Optional<McpUserPrincipal> currentUser(ToolContext toolContext) {
        if (toolContext == null) {
            return Optional.empty();
        }
        return McpToolUtils.getMcpExchange(toolContext)
                .map(McpSyncServerExchange::transportContext)
                .map(context -> context.get(PRINCIPAL_KEY))
                .filter(McpUserPrincipal.class::isInstance)
                .map(McpUserPrincipal.class::cast);
    }
}
