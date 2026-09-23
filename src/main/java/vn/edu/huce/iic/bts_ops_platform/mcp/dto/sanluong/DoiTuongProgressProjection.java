package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import java.math.BigDecimal;
import java.util.UUID;

public interface DoiTuongProgressProjection {
    UUID getDoiTuongId();
    String getDoiTuongTen();
    UUID getHopDongId();
    String getHopDongTen();
    UUID getNhaThauId();
    String getNhaThauTen();
    UUID getKhuVucId();
    String getKhuVucTen();
    Boolean getHasOpenVuongMac();
    BigDecimal getPeriodValue();
    Integer getTotalHangMuc();
    Integer getDoneHangMuc();
}
