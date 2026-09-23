package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.McpUserContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Bảo vệ MCP tool server và các endpoint debug liên quan (ATTT PL06.2#1: API server-server phải xác
 * thực). Yêu cầu header {@code X-API-Key} khớp {@code app.mcp.api-key} (env {@code
 * APP_MCP_API_KEY}).
 *
 * <p>Fail-closed: nếu CHƯA cấu hình khóa thì chặn toàn bộ (503) thay vì mở toang.
 *
 * <p>Danh tính người dùng (id/quyền/khu vực) không còn xác thực bằng JWT — caller đã xác thực
 * người dùng ở phía mình và tự gắn kèm các header {@code X-User-*} (xem {@link McpUserContext}); ở
 * đây chỉ đọc và tin tưởng, miễn X-API-Key đúng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";
    private static final Set<String> PROTECTED_PREFIXES = Set.of("/mcp", "/api/v1/tools", "/api/v1/mcp");

    private final McpUserContext userContext;

    @Value("${app.mcp.api-key:}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PROTECTED_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("MCP request bị từ chối: chưa cấu hình app.mcp.api-key (fail-closed)");
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "MCP server chưa được cấu hình khóa truy cập");
            return;
        }
        String provided = request.getHeader(HEADER);
        if (provided == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8))) {
            log.warn("MCP request bị từ chối: X-API-Key không hợp lệ, uri={}", request.getRequestURI());
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Thiếu hoặc sai X-API-Key");
            return;
        }

        userContext.resolve(request).ifPresent(user -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            // Đưa username vào MDC cho log file (ATTT PL14#16) — xóa ở finally tránh rò rỉ
            // sang request khác dùng lại thread từ pool.
            MDC.put("username", user.tenDangNhap());
        });
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("username");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"isSuccess\":false,\"message\":\"" + message + "\",\"errors\":{\"type\":\"UNAUTHORIZED\"}}");
    }
}
