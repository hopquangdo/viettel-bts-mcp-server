package vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

/** Kết quả tool hợp nhất bienban_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienBanQueryResponse {
    /** Tổng quan số lượng theo trạng thái duyệt + theo loại — luôn có. */
    private BienBanTongQuanToolItem tongQuan;
    private PagedResult<BienBanThieuKhaoSatToolItem> thieuKhaoSat;
    private PagedResult<BienBanThieuTheoTienDoToolItem> thieuHoSo;
    private PagedResult<BienBanTheoTrangThaiToolItem> theoTrangThai;
}
