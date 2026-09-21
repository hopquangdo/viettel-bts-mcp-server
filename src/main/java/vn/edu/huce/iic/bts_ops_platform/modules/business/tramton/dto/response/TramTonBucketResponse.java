package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonBucketResponse {
    private String id;
    private String label;
    private long count;
    private BigDecimal value;
    private double percent;
}
