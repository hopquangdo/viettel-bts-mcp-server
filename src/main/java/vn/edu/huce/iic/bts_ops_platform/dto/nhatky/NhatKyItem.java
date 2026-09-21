package vn.edu.huce.iic.bts_ops_platform.dto.nhatky;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;

import java.time.Instant;

/** 1 thao tác trong nhật ký: ai làm gì, lúc nào, trên hợp đồng/đối tượng nào. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhatKyItem {
    private Instant thoiGian;
    private String nguoiThucHien;
    private String hanhDong;
    /** Nhãn tiếng Việt của hành động (vd "Nhập sản lượng"). */
    private String tenHanhDong;
    /** Nội dung chi tiết, với thao tác sửa có dạng "trường cũ → mới". */
    private String moTa;
    private String ip;
    private HopDongInfo hopDong;
    /** Loại đối tượng (Trạm, Tuyến…). */
    private String loaiDoiTuong;
    /** Giá trị khoá chính của đối tượng cụ thể (mã trạm, mã tuyến…), nếu xác định được. */
    private String maDoiTuong;
}
