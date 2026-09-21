package vn.edu.huce.iic.bts_ops_platform.infrastructure.events.helpers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventMetadata;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.logging.RequestMdcLoggingFilter;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;

@Component
public class AppEventMetadataResolver {

    public AppEventMetadata resolve(String source) {
        JwtUserPrincipal principal = getCurrentPrincipal();
        return new AppEventMetadata(
                principal != null ? principal.id() : null,
                principal != null ? principal.hoTen() : null,
                MDC.get(RequestMdcLoggingFilter.MDC_REQUEST_ID),
                resolveClientIp(),
                source);
    }

    private JwtUserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal;
        }
        return null;
    }

    private String resolveClientIp() {
        String fromMdc = MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
