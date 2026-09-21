package vn.edu.huce.iic.bts_ops_platform.dto.common;

import java.math.BigDecimal;

/** Xếp hạng 1 thực thể (nhà thầu, khu vực...) kèm chỉ số — dùng chung cho mọi tool RANK/xếp hạng. */
public record RankedEntityDto(
        String id,
        String name,
        BigDecimal value,
        BigDecimal completionRate,
        int objectCount,
        int issueCount,
        Integer rank
) {
}
