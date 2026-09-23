package vn.edu.huce.iic.bts_ops_platform.mcp.dto.vuongmac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

import java.util.List;

/** Kết quả tool hợp nhất vuongmac_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VuongMacQueryResponse {
    private VuongMacTongQuanDto tongQuan;
    private PagedResult<VuongMacItemDto> danhSach;
    private List<RankedItemDto> xepHangHopDong;
    /** Xếp hạng khu vực nhiều vướng mắc mở nhất (top N, mặc định 5) — luôn có. */
    private List<RankedItemDto> xepHangKhuVuc;
    private PagedResult<VuongMacItemDto> quaHan;
}
