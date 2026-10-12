package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.response;

import java.util.UUID;

/** A contract below the yellow progress threshold. */
public record HopDongCanhBaoItemResponse(
        UUID hopDongId,
        String maHopDong,
        String tenHopDong,
        double tyLeHoanThanh) {
}