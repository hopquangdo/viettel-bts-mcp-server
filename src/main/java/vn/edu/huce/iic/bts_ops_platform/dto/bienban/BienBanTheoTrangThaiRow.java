package vn.edu.huce.iic.bts_ops_platform.dto.bienban;

import java.time.Instant;
import java.time.LocalDate;

public interface BienBanTheoTrangThaiRow {
    String getMaBienBan();
    String getLoaiBienBan();
    String getMaHopDong();
    String getTenHopDong();
    String getTrangThai();
    LocalDate getNgayLap();
    String getNguoiLapTen();
    String getLyDoTuChoi();
    String getNguoiPheDuyetTen();
    Instant getNgayPheDuyet();
}
