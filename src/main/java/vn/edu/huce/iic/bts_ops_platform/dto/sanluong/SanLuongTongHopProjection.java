package vn.edu.huce.iic.bts_ops_platform.dto.sanluong;

import java.math.BigDecimal;

public interface SanLuongTongHopProjection {
    Integer getTotalDisplayed();
    Integer getWithOutput();
    BigDecimal getPeriodTotal();
    BigDecimal getTodayTotal();
    /** Tổng giá trị sản lượng hoàn thành của kỳ liền trước (cùng bộ lọc) — dùng so sánh xu hướng. */
    BigDecimal getPreviousTotal();
}
