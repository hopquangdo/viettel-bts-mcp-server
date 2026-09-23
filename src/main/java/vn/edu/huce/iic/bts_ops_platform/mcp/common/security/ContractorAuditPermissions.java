package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.openapi.QuyenHanMa;

import java.util.Set;

/** Quyền kiểm duyệt/phê duyệt chất lượng — nhà thầu không được dùng dù ma trận có bật. */
public final class ContractorAuditPermissions {

    private static final Set<String> AUDIT_PERMISSIONS = Set.of(
            QuyenHanMa.NGHIEM_THU_SAN_LUONG,
            QuyenHanMa.PHE_DUYET_BIEN_BAN,
            QuyenHanMa.SUA_SO_LIEU_DA_DUYET);

    private ContractorAuditPermissions() {
    }

    public static boolean isAuditPermission(String quyenHanMa) {
        return quyenHanMa != null && AUDIT_PERMISSIONS.contains(quyenHanMa);
    }
}
