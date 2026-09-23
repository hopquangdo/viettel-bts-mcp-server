package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nhóm các đối tượng có cùng (hợp đồng, nhà thầu, khu vực, cờ vướng mắc, giá trị kỳ, tổng hạng mục, hạng mục hoàn thành):
 * {@link #getN()} là số đối tượng trong nhóm. Thay cho việc kéo từng dòng đối tượng (hàng nghìn dòng) về Java chỉ để cộng dồn.
 */
public interface ProgressGroupProjection {
    UUID getHopDongId();
    String getHopDongTen();
    UUID getNhaThauId();
    String getNhaThauTen();
    UUID getKhuVucId();
    String getKhuVucTen();
    UUID getTinhId();
    String getTinhTen();
    UUID getLoaiHopDongId();
    String getLoaiHopDongTen();
    Boolean getHasOpenVuongMac();
    BigDecimal getPeriodValue();
    Integer getTotalHangMuc();
    Integer getDoneHangMuc();
    Long getN();
}
