package vn.edu.huce.iic.bts_ops_platform.common.security;

/** Mã quyền "Chủ đầu tư" — song song với {@link ContractorRoleCodes} (Nhà thầu, mã "04"). */
public final class InvestorRoleCodes {

    public static final String MA = "05";

    private InvestorRoleCodes() {
    }

    public static boolean isInvestor(String quyenMa) {
        if (quyenMa == null || quyenMa.isBlank()) {
            return false;
        }
        return MA.equals(quyenMa.trim());
    }
}
