package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong;

import java.time.LocalDate;

public interface KhoiCongTrongKyProjection {
    String getMaDoiTuong();
    String getTenDoiTuong();
    String getMaHopDong();
    LocalDate getNgayKhoiCong();
}
