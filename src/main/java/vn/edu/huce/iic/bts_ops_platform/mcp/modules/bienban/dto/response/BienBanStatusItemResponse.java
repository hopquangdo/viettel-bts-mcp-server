package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.response;

import java.time.Instant;
import java.time.LocalDate;

/** Biên bản status-list item, including approval and rejection details when present. */
public record BienBanStatusItemResponse(
        String maBienBan,
        String loaiBienBan,
        String maHopDong,
        String tenHopDong,
        String trangThai,
        LocalDate ngayLap,
        String nguoiLapTen,
        String lyDoTuChoi,
        String nguoiPheDuyetTen,
        Instant ngayPheDuyet) {
}