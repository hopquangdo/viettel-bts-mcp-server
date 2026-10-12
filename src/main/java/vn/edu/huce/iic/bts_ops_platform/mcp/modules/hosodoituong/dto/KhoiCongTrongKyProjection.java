package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto;

import java.time.LocalDate;

public interface KhoiCongTrongKyProjection {
    String getMaDoiTuong();
    String getTenDoiTuong();
    String getMaHopDong();
    String getKhuVuc();
    String getNhaThau();
    LocalDate getNgayKhoiCong();
}
