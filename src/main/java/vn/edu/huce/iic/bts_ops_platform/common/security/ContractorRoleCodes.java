package vn.edu.huce.iic.bts_ops_platform.common.security;

import java.util.Locale;

public final class ContractorRoleCodes {

    public static final String MA = "04";

    private ContractorRoleCodes() {
    }

    public static boolean isContractor(String quyenMa) {
        if (quyenMa == null || quyenMa.isBlank()) {
            return false;
        }
        return MA.equals(quyenMa.trim());
    }

    public static boolean isContractorRoleAuthority(String authority) {
        return authority != null && AuthorityPrefix.roleQuyen(MA).equals(authority.toUpperCase(Locale.ROOT));
    }
}
