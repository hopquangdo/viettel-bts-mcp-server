package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

public final class AuthorityPrefix {

    public static final String ROLE = "ROLE_";
    public static final String QUYEN = "QUYEN_";
    public static final String HAN = "HAN_";

    /** Mã quyền hạn wildcard trong seed — đại diện cho quyền truy cập toàn bộ dữ liệu. */
    public static final String FULL_ACCESS = "FULL_ACCESS";

    private AuthorityPrefix() {
    }

    public static String roleQuyen(String quyenMa) {
        return QUYEN + quyenMa.toUpperCase();
    }

    public static String quyenHan(String quyenHanMa) {
        return HAN + quyenHanMa.toUpperCase();
    }

    public static String fullAccessQuyenHan() {
        return quyenHan(FULL_ACCESS);
    }

    public static boolean isFullAccessAuthority(String authority) {
        return fullAccessQuyenHan().equals(authority);
    }
}
