package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response;

import java.time.Instant;
import java.util.UUID;

/** One assignment returned by the module's list operation. */
public record PhanCongItemResponse(
        UUID id,
        UUID hopDongId,
        UUID hopDongDoiTuongId,
        String nhaThau,
        String giaiDoan,
        UUID khuVucId,
        String maVung,
        UUID tinhThanhId,
        Boolean hoatDong,
        Instant ngayTao) {
}