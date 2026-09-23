package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

import java.time.Instant;

/** Kết quả auditlog_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhatKyQueryResponse {
    /** Tổng số thao tác khớp bộ lọc. */
    private long tongSo;
    /** Thao tác sớm nhất khớp bộ lọc (vd người nhập sản lượng lần đầu). */
    private NhatKyItem lanDau;
    /** Thao tác mới nhất khớp bộ lọc (vd người sửa gần nhất). */
    private NhatKyItem ganNhat;
    private PagedResult<NhatKyItem> danhSach;
    /** Thời điểm sớm nhất có nhật ký thao tác sản lượng; sản lượng nhập/sửa trước mốc này không có người thực hiện. Null nếu chưa có. */
    private Instant sanLuongGhiTuLuc;
    /** Lưu ý cho người đọc (ví dụ giới hạn dữ liệu cũ). */
    private String luuY;
}
