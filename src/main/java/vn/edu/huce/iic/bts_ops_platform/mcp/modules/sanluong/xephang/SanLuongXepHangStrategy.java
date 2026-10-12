package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;

/** Xếp hạng sản lượng theo 1 chiều. Mỗi chiều 1 {@code @Component}, gom ở {@link SanLuongXepHangRegistry}. */
public interface SanLuongXepHangStrategy {

    SanLuongXepHangChieu chieu();

    RankingOverviewDto rank(SanLuongXepHangContext ctx);

    /** Lý do bỏ qua chiều này trong phạm vi hiện tại (tiếng Việt), null nếu xếp hạng được. */
    default String lyDoBoQua(SanLuongXepHangContext ctx) {
        if (ctx.capDaLoc().contains(chieu())) {
            String ten = chieu().label().toLowerCase();
            return "Đang lọc theo 1 " + ten + " cụ thể nên bảng xếp hạng theo " + ten + " chỉ có 1 dòng, đã bỏ qua.";
        }
        return null;
    }
}
