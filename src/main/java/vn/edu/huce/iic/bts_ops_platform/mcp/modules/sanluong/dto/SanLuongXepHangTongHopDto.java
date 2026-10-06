package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;

/**
 * Nội bộ: mọi bảng xếp hạng sản lượng trong 1 lần, chỉ cho đường tổng quan ({@code SanLuongToolHandler.query}).
 * Bảng cùng cấp với bộ lọc là null; lọc 1 đối tượng cụ thể thì mọi bảng null. Tool sanluong_xep_hang dùng
 * {@link SanLuongXepHangResponse} (1 chiều mỗi lần gọi).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongXepHangTongHopDto(
        PeriodInfo period,
        RankingOverviewDto nhaThau,
        RankingOverviewDto khuVuc,
        RankingOverviewDto tinh,
        RankingOverviewDto hopDong,
        RankingOverviewDto loaiHopDong,
        RankingOverviewDto doiTuong) {
}
