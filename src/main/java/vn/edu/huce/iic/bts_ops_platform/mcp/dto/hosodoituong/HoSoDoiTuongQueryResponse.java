package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

/** Kết quả tool hợp nhất hosodoituong_tool — các field null nếu không áp dụng với filter đã truyền. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoSoDoiTuongQueryResponse {
    /** Chỉ có khi truyền maHopDong. */
    private HoSoDoiTuongTongQuanDto tongQuan;
    /** Chỉ có khi truyền maDoiTuong. */
    private HoSoDoiTuongThuocTinhDto thuocTinh;
    private PagedResult<HoSoDoiTuongChuaKhaoSatItem> chuaKhaoSat;
    private PagedResult<HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem> khaoSatXongChuaCoSanLuong;
}
