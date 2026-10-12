package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodTrendDto;

/** sanluong_xu_huong: giá trị sản lượng theo từng kỳ con (ngày/tuần/tháng tự chọn theo độ dài khoảng ngày). */
public record SanLuongXuHuongResponse(
        PeriodInfo period,
        PeriodTrendDto<SanLuongPeriodTrendDto> periodTrend) {
}
