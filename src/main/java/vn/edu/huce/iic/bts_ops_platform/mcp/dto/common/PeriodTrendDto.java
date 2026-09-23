package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodTrendDto<T> {

    private String fromDate;
    private String toDate;
    private String granularity;
    private List<T> values;
}
