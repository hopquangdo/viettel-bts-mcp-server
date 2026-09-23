package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Kết quả tool hợp nhất hopdong_tool — các field null nếu không áp dụng với filter đã truyền. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongQueryResponse {
    /** Tổng quan hệ thống — luôn có, breakdown theo trạng thái/loại/kiểu. */
    private HopDongTongQuanResponse tongQuan;
    /** Danh sách hợp đồng khớp filter (query/nhaThauId/maTram) — chỉ có khi cần tra cứu danh sách. */
    private HopDongTimKiemResponse danhSach;
    /** Thống kê thu hẹp theo khu vực/nhà thầu/1 hợp đồng cụ thể — chỉ có khi truyền khuVuc/nhaThauId/maHopDong. */
    private HopDongTrangThaiKhuVucResponse thongKeThuHep;
    /** Phân loại Xanh/Vàng/Đỏ theo tiến độ — luôn có. */
    private HopDongCanhBaoTienDoResponse canhBaoTienDo;
    /** Xếp hạng khu vực theo số hợp đồng/đối tượng — luôn có. */
    private List<HopDongXepHangKhuVucToolItem> xepHangKhuVuc;
    /** Xếp hạng tỉnh trong 1 hợp đồng — chỉ có khi truyền maHopDong. */
    private List<HopDongXepHangTinhToolItem> xepHangTinh;
    /** Phân bố + lịch sử chuyển bước — chỉ có khi truyền maHopDong. */
    private HopDongTheoBuocResponse theoBuoc;
    /** Số đối tượng hoàn thành thi công trong khoảng fromDate-toDate — chỉ có khi truyền fromDate/toDate. */
    private Long doiTuongHoanThanhTrongKy;
}
