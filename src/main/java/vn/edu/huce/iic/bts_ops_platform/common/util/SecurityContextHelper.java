package vn.edu.huce.iic.bts_ops_platform.common.util;

import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;

public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static JwtUserPrincipal requireCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal;
        }
        throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Chưa đăng nhập");
    }

    public static JwtUserPrincipal currentUserOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal;
        }
        return null;
    }
}
