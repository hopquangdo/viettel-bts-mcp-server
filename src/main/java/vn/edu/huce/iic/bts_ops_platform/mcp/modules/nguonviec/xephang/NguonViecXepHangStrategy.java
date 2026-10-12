package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;

import java.util.List;

/**
 * Xếp hạng nguồn việc theo 1 chiều trên tập hợp đồng đã lấy sẵn (không truy vấn DB).
 * Mỗi chiều 1 {@code @Component}, gom ở {@link NguonViecXepHangRegistry}.
 */
public interface NguonViecXepHangStrategy {

    NguonViecXepHangChieu chieu();

    /** Tiêu chí sắp xếp đọc được, vd "Điểm rủi ro, cao xuống thấp". */
    String tieuChi();

    /** {@code top}: số dòng tối đa (chiều nào tự giới hạn thì bỏ qua). */
    List<RankedItemResponse> rank(List<NguonViecRowToolItem> rows, int top);
}
