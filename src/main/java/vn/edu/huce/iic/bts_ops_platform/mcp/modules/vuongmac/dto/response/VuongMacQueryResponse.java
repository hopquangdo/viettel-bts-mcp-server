package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacItemDto;

import java.util.List;

/** Kết quả tool hợp nhất vuongmac_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VuongMacQueryResponse {
    private VuongMacTongQuanResponse tongQuan;
    private PagedResult<VuongMacItemDto> danhSach;
    private List<RankedItemDto> xepHangHopDong;
    private List<RankedItemDto> xepHangKhuVuc;
    private PagedResult<VuongMacItemDto> quaHan;
}
