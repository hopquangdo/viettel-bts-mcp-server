package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau;

import java.util.UUID;

/**
 * Tham chiếu id + tên của 1 nhà thầu — dùng cả để nhúng vào response (thay flatten
 * nhaThauId/tenNhaThau) và làm kết quả resolve filter của NhaThauComponent.
 */
public record NhaThauInfo(UUID id, String ten) {

    public static NhaThauInfo of(UUID id, String ten) {
        if (id == null && (ten == null || ten.isBlank())) {
            return null;
        }
        return new NhaThauInfo(id, ten);
    }
}
