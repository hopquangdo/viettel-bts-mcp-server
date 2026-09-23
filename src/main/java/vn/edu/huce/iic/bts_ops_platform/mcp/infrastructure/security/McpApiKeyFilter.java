package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Bảo vệ MCP tool server (ATTT PL06.2#1: API server-server phải xác thực) — trước đây {@code
 * /mcp/**} để permitAll không kiểm soát. Yêu cầu header {@code X-API-Key} khớp {@code
 * app.mcp.api-key} (env {@code APP_MCP_API_KEY}).
 *
 * <p>Fail-closed: nếu CHƯA cấu hình khóa thì chặn toàn bộ /mcp (503) thay vì mở toang — MCP hiện
 * chưa dùng chính thức, muốn bật thì phải đặt khóa trước.
 */
@Slf4j
@Component
public class McpApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    @Value("${app.mcp.api-key:}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/mcp");
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
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"isSuccess\":false,\"message\":\"" + message + "\",\"errors\":{\"type\":\"UNAUTHORIZED\"}}");
    }
}
