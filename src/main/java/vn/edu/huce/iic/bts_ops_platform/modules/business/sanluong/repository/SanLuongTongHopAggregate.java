package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository;

import java.math.BigDecimal;

public interface SanLuongTongHopAggregate {

    Integer getTotalDisplayed();

    Integer getWithOutput();

    BigDecimal getPeriodTotal();

    BigDecimal getTodayTotal();
}
