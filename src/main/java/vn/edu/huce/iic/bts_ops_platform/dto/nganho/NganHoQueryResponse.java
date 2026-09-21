package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kết quả tool hợp nhất volume_tool — wrapper mỏng trên VolumeService (module Volume), ánh xạ sang DTO riêng của tools. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoQueryResponse {
    /** Tổng quan đối soát giá trị HĐ vs thành tiền thi công: số HĐ vượt ngưỡng / thiếu / thừa /
     * cân bằng, tỷ lệ sử dụng, chênh lệch — luôn có. */
    private NganHoTongQuanToolDto tongQuan;
    /** Chi tiết mức tiêu thụ 1 hợp đồng (% đã tiêu, breakdown theo nhóm) — chỉ có khi truyền maHopDong. */
    private NganHoChiTietHopDongToolDto chiTietHopDong;
    /** Phân bổ theo khu vực / tỉnh của 1 hợp đồng, kèm cảnh báo tỉnh thừa / thiếu — chỉ có khi truyền maHopDong. */
    private NganHoKhuVucToolDto theoKhuVucTinh;
    /** Danh sách hợp đồng theo trạng thái (thieu/thua/can_bang/alert/da_qt/dang_qt/chua_qt) và loại hợp đồng — chỉ có khi truyền statusFilter hoặc loaiHopDong. */
    private vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult<NganHoHopDongToolItem> danhSachHopDong;
}
