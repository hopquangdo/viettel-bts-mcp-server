package vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

import java.util.List;

/** Kết quả tool hợp nhất phancong_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhanCongQueryResponse {
    private PhanCongToolThongKe tongQuan;
    private PagedResult<PhanCongToolItem> danhSach;
    private PagedResult<PhanCongToolItem> theoCanBo;
    private PagedResult<PhanCongChuaPhanCongToolItem> chuaPhanCong;
    /** Xếp hạng nhà thầu theo số đối tượng phụ trách (top N, mặc định 5) — luôn có. */
    private List<PhanCongXepHangNhaThauToolItem> xepHangNhaThau;
}
