package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

/**
 * Kết quả nghiệm thu sản lượng theo bộ lọc (khoảng ngày lọc theo ngày nghiệm thu, không lọc thì lấy toàn bộ thời gian).
 * Hạng mục nghiệm thu không đạt bị loại khỏi sản lượng đã tính (hoạt động = false) nên không nằm trong các số liệu sản lượng khác.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongNghiemThuDto {
    /** Hạng mục nghiệm thu đạt (= đã duyệt) và tổng giá trị (khối lượng x đơn giá), lọc ngày theo ngày nghiệm thu. */
    private long soDat;
    private java.math.BigDecimal giaTriDat;
    private long soKhongDat;
    private java.math.BigDecimal giaTriKhongDat;
    /** Hạng mục đã báo hoàn thành (done) nhưng CHƯA nghiệm thu, và tổng giá trị; lọc ngày theo ngày thực hiện (toàn bộ thời gian nếu không truyền ngày). */
    private long soChoNghiemThu;
    private java.math.BigDecimal giaTriChoNghiemThu;
    private PagedResult<SanLuongNghiemThuKhongDatItem> khongDat;
}
