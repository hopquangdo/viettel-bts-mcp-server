package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/** Tổng thành tiền sản lượng gộp theo đối tượng hợp đồng. */
public record SanLuongDoiTuongTongResponse(
        UUID hopDongDoiTuongId,
        UUID hopDongId,
        BigDecimal tongThanhTien) {
}
