package vn.edu.huce.iic.bts_ops_platform.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestMdcLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CLIENT_IP, resolveClientIp(request));
        response.setHeader(REQUEST_ID_HEADER, requestId);
        RequestTraceContext.markRequestStart();

        long startNanos = System.nanoTime();
        try {
            log.info(
                    "request started method={} uri={}{}",
                    request.getMethod(),
                    request.getRequestURI(),
                    queryStringSuffix(request));
            filterChain.doFilter(request, response);
        } finally {
            long endNanos = System.nanoTime();
            log.info(
                    "request completed method={} uri={} status={} duration={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    RequestTraceContext.formatDuration(endNanos - startNanos));
            RequestTraceContext.clear();
            MDC.clear();
        }
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader(REQUEST_ID_HEADER);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return UUID.randomUUID().toString();
    }

    private static String queryStringSuffix(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? "" : "?" + query;
    }
}
