package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Số đối tượng thiếu từng loại thuộc tính khảo sát trong 1 hợp đồng — chỉ tính trên dữ liệu THỰC
 * SỰ có trong DB (ngay_ban_giao_mat_bang, trang_thai_vat_tu_a/b, ngay_hoan_thanh_vat_tu_b). KHÔNG
 * có loại cột / chiều cao / ảnh thi công / ngày khảo sát — hệ thống chưa lưu các trường này.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoSoDoiTuongTongQuanDto {
    private long tongDoiTuong;
    /** Chưa có ngày bàn giao mặt bằng — proxy gần nhất cho "chưa khảo sát thực địa". */
    private long thieuBanGiaoMatBang;
    /** trang_thai_vat_tu_a khác 'DA_DAM_BAO'. */
    private long vatTuAChuaDamBao;
    /** Chưa có ngày hoàn thành vật tư B. */
    private long vatTuBChuaHoanThanh;
}
