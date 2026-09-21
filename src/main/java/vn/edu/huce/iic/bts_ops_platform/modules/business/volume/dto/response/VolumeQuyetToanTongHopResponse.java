package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Tổng quyết toán (SUM các đợt còn hoạt động) + danh sách đợt — trả về sau mỗi lần list/thêm/sửa/xóa. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeQuyetToanTongHopResponse {
    private BigDecimal tongQuyetToan;
    private List<VolumeQuyetToanDotResponse> dots;
    /** Khóa thêm/sửa/xóa đợt (trạng thái tiến độ quyết toán). */
    private Boolean quyetToanDotFullLock;
    /** Khóa sửa/xóa đợt (đã có quyết toán thực hoặc full lock). */
    private Boolean quyetToanDotEditLock;
    private String quyetToanDotLockMessage;
}
