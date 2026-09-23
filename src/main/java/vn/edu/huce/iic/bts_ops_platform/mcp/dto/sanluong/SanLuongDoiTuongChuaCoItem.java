package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 1 đối tượng đang hoạt động chưa ghi nhận sản lượng hoàn thành nào trong khoảng ngày lọc. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongDoiTuongChuaCoItem {
    /** Giá trị khoá chính của đối tượng (mã trạm, mã tuyến…), nếu xác định được. */
    private String maDoiTuong;
    /** Loại đối tượng (Trạm, Tuyến…). */
    private String loaiDoiTuong;
    private String hopDong;
    private String nhaThau;
    private String khuVuc;
    /** Tổng số hạng mục áp dụng cho đối tượng (theo hợp đồng). */
    private int soHangMuc;
    /** Có vướng mắc đang mở hay không. */
    private boolean coVuongMacMo;
}
