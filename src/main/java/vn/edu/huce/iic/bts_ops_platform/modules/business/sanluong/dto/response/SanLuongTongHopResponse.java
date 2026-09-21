package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SanLuongTongHopResponse {
    private int totalDisplayed;
    private int withOutput;
    private BigDecimal periodTotal;
    private BigDecimal todayTotal;
    private int issueCount;
    private BigDecimal averagePerDoiTuong;
    private BigDecimal periodGrowthPercent;
}
