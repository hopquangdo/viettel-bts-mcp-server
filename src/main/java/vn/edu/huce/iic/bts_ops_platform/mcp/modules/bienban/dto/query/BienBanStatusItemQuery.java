package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.query;

import java.time.Instant;
import java.time.LocalDate;

/** Persistence projection for one Biên bản status-list row. */
public record BienBanStatusItemQuery(
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