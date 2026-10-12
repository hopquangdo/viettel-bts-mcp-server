package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import java.util.List;

/** Xếp hạng hợp đồng theo 1 chiều. Mỗi chiều 1 {@code @Component}, gom ở {@link HopDongXepHangRegistry}. */
public interface HopDongXepHangStrategy {

    HopDongXepHangChieu chieu();

    /** Tiêu chí sắp xếp đọc được, vd "Số hợp đồng, nhiều xuống ít". */
    String tieuChi();

    /** Lý do bỏ qua chiều này trong phạm vi hiện tại (tiếng Việt), null nếu xếp hạng được. */
    String lyDoBoQua(HopDongXepHangContext ctx);

    /** Chạy 1 truy vấn lá; gọi trong {@code McpParallel.async}. */
    List<HopDongXepHangDong> rank(HopDongXepHangContext ctx);
}
