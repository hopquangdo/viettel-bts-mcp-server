package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.response;

import java.util.List;

/** Response for the contract progress-warning tool. */
public record HopDongCanhBaoTienDoResponse(
        long soHopDongXanh,
        long soHopDongVang,
        long soHopDongDo,
        List<HopDongCanhBaoItemResponse> hopDongDo) {
}