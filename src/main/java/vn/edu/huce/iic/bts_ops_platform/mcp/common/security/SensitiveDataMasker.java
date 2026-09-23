package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguoidung.NguoiDungResponse;

/**
 * Làm mờ dữ liệu nhạy cảm theo phân loại danh mục trọng yếu (email, SĐT).
 */
@Component
public class SensitiveDataMasker {

    public NguoiDungResponse maskIfNeeded(NguoiDungResponse response) {
        if (response == null || canViewSensitive()) {
            return response;
        }
        if (response.getEmail() != null && !response.getEmail().isBlank()) {
            response.setEmail(maskEmail(response.getEmail()));
        }
        if (response.getDienThoai() != null && !response.getDienThoai().isBlank()) {
            response.setDienThoai(maskPhone(response.getDienThoai()));
        }
        return response;
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 1) {
            return "*" + domain;
        }
        return local.charAt(0) + "***" + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }

    private static boolean canViewSensitive() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return true;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> AuthorityPrefix.quyenHan(QuyenHanMa.QUAN_LY_NGUOI_DUNG).equals(a)
                        || AuthorityPrefix.isFullAccessAuthority(a)
                        || FullAccessRoleCodes.isFullAccessRoleAuthority(a));
    }
}
