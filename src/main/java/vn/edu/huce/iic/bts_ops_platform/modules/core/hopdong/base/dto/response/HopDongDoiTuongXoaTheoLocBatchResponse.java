package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HopDongDoiTuongXoaTheoLocBatchResponse {
    private int deletedCount;
    private long total;
    private boolean hasMore;
}
