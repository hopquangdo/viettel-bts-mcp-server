package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Builder;
import lombok.Data;

/** Số lượng hợp đồng nguồn việc theo nhóm tab — mirror NguonViecTabCountsResponse (module NguonLuc). */
@Data
@Builder
public class NguonViecTabCountsToolDto {
    private long all;
    private long active;
    private long nearly;
}
