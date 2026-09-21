package vn.edu.huce.iic.bts_ops_platform.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PhanQuyenResolverService phanQuyenResolverService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = getBearerToken(request);
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtUserPrincipal principal = jwtService.verifyToken(token, JwtService.LOAI_ACCESS);
                JwtUserPrincipal enriched = principal.withAuthorities(
                        phanQuyenResolverService.resolveAuthorities(principal.quyenId()));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(enriched, null, enriched.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Đưa username vào MDC cho log file (ATTT PL14#16) — xóa ở finally tránh rò rỉ
                // sang request khác dùng lại thread từ pool.
                MDC.put("username", enriched.tenDangNhap());
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("username");
        }
    }

    private String getBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        if (request.getRequestURI() != null && request.getRequestURI().contains("/realtime/stream")) {
            String queryToken = request.getParameter("access_token");
            if (StringUtils.hasText(queryToken)) {
                return queryToken.trim();
            }
        }
        return null;
    }
}
