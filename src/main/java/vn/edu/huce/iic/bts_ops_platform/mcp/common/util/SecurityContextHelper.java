package vn.edu.huce.iic.bts_ops_platform.mcp.common.util;

import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.McpUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.AuthErrorCode;

public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static McpUserPrincipal requireCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof McpUserPrincipal mcpUserPrincipal) {
            return mcpUserPrincipal;
        }
        throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Chưa đăng nhập");
    }

    public static McpUserPrincipal currentUserOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof McpUserPrincipal mcpUserPrincipal) {
            return mcpUserPrincipal;
        }
        return null;
    }
}
