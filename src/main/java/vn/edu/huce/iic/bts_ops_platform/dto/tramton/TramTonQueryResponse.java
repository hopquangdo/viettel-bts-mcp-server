package vn.edu.huce.iic.bts_ops_platform.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;

import java.util.List;

/** Kết quả tool hợp nhất tramton_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonQueryResponse {
    private TramTonToolTongQuanDto tongQuan;
    private PagedResult<TramTonToolItemDto> danhSach;
    private List<TramTonToolThieuCapNhatItemDto> thieuCapNhat;
    private List<TramTonToolSanLuongBatThuongItemDto> sanLuongBatThuong;
    /** Xếp hạng khu vực theo số đối tượng tồn chờ quyết toán (top N, mặc định 5) — luôn có. */
    private List<TramTonXepHangKhuVucToolItem> xepHangKhuVuc;
    /** Đối tượng thiếu từ 2 điều kiện quyết toán trở lên — tối đa 20, luôn có. */
    private List<TramTonThieuNhieuDieuKienToolItem> thieuNhieuDieuKien;
}
