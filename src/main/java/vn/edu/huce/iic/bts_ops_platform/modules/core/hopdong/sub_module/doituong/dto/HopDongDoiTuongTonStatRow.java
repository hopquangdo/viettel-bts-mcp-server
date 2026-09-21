package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Bản rút gọn của HopDongDoiTuongTonItemResponse — chỉ giữ đúng field cần cho thống kê (đếm/
 * bucket tuổi tồn/tổng giá trị). Bỏ hẳn mã trạm/khu vực/nhà thầu (không resolve snapshot) vì
 * tongQuan() không hiển thị từng dòng, chỉ đếm — xem HopDongDoiTuongTonService.choQuyetToanStatsAll.
 */
public record HopDongDoiTuongTonStatRow(UUID id, long soNgayTon, BigDecimal giaTri) {
}
