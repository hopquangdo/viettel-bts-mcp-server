package vn.edu.huce.iic.bts_ops_platform.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Tổng quan xếp hạng 1 nhóm thực thể (nhà thầu, khu vực...) — dùng chung cho mọi tool OVERVIEW/RANK cần top/bottom. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingOverviewDto {
    private int totalEntities;
    private RankedEntityDto topByOutput;
    private RankedEntityDto topByCompletion;
    private RankedEntityDto bottomByCompletion;
    private List<RankedEntityDto> rankings;
}
