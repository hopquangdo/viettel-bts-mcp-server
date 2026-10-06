package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query;

import java.time.Instant;
import java.util.UUID;

/** Repository projection for one assignment row. */
public record PhanCongRow(
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