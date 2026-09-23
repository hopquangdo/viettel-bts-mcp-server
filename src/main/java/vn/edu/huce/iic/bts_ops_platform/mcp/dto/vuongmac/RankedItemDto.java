package vn.edu.huce.iic.bts_ops_platform.mcp.dto.vuongmac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 1 dòng trong danh sách xếp hạng top-N — dùng cho vuongmac_top_hopdong. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedItemDto {
    /** Mã/tên định danh ngắn dùng để hiển thị & lọc tiếp (vd mã hợp đồng, tên khu vực). */
    private String label;
    /** Tên đầy đủ của thực thể ứng với `label`, nếu có (vd tên hợp đồng) — null nếu label đã là tên. */
    private String name;
    private long value;
}
