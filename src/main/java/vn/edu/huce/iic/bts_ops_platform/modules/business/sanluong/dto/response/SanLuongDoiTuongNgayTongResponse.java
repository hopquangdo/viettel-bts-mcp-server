package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Tổng thành tiền sản lượng gộp theo đối tượng hợp đồng và ngày thực hiện. */
public record SanLuongDoiTuongNgayTongResponse(
        UUID hopDongDoiTuongId,
        LocalDate ngay,
        BigDecimal tongThanhTien) {
}
