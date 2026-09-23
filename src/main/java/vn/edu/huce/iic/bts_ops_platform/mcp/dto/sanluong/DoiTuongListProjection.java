package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** 1 dòng của danh sách đối tượng (trang Sản lượng): thông tin đối tượng, sản lượng lũy kế/hôm nay/trong kỳ, tiến độ hạng mục. */
public interface DoiTuongListProjection {
    UUID getDoiTuongId();
    String getMaDoiTuong();
    String getDoiTuongTen();
    String getHopDongTen();
    String getNhaThauTen();
    String getKhuVucTen();
    String getTinhMa();
    String getTinhTen();
    Boolean getLaTinhCu();
    Boolean getHasOpenVuongMac();
    BigDecimal getPeriodValue();
    BigDecimal getTotalValue();
    BigDecimal getTodayValue();
    LocalDate getThiCongGanNhat();
    Integer getTotalHangMuc();
    Integer getDoneHangMuc();
    Long getTong();
}
