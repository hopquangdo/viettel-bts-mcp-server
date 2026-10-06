package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;

import java.util.List;

/** Xếp hạng số vướng mắc đang mở theo 1 chiều. Mỗi chiều 1 {@code @Component}, gom ở {@link VuongMacXepHangRegistry}. */
public interface VuongMacXepHangStrategy {

    VuongMacXepHangChieu chieu();

    /** Lý do bỏ qua chiều này trong phạm vi hiện tại (tiếng Việt), null nếu xếp hạng được. */
    String lyDoBoQua(VuongMacXepHangContext ctx);

    /** Chạy 1 truy vấn lá; gọi trong {@code McpParallel.async}. */
    List<RankedItemDto> rank(VuongMacXepHangContext ctx);
}
