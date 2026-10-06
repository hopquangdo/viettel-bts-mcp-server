package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.OutputSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ProgressSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.TrendInfo;

import java.math.BigDecimal;

/**
 * sanluong_tong_quan: tổng giá trị trong kỳ (summary.periodValue = tiền đã báo), tiến độ hạng mục, so với kỳ trước.
 * thanhTienHoanThanh / thanhTienHoanThanhKyTruoc: thành tiền sản lượng hoàn thành (công thức Dashboard) trong kỳ và kỳ trước.
 */
public record SanLuongTongQuanResponse(
        PeriodInfo period,
        OutputSummaryDto summary,
        ProgressSummaryDto progress,
        TrendInfo trend,
        BigDecimal thanhTienHoanThanh,
        BigDecimal thanhTienHoanThanhKyTruoc) {

    public SanLuongTongQuanResponse(PeriodInfo period, OutputSummaryDto summary, ProgressSummaryDto progress, TrendInfo trend) {
        this(period, summary, progress, trend, null, null);
    }
}
