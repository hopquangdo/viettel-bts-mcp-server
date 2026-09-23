package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import java.math.BigDecimal;
import java.util.UUID;

public interface NhaThauDaBaoProjection {
    UUID getNhaThauId();
    String getTenNhaThau();
    Long getSoDoiTuongPhuTrach();
    BigDecimal getGiaTriDaBao();
    /** Tổng số nhà thầu đã báo (cùng bộ lọc, trước khi phân trang). */
    Long getTong();
}
