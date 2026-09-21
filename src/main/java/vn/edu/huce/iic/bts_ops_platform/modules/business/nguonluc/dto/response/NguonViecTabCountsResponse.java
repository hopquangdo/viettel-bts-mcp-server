package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NguonViecTabCountsResponse {
    private long all;
    private long active;
    private long nearly;
}
