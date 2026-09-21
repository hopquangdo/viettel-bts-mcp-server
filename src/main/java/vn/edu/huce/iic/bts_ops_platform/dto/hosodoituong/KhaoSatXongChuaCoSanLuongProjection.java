package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import java.time.LocalDate;

public interface KhaoSatXongChuaCoSanLuongProjection {
    String getMaDoiTuong();
    String getTenDoiTuong();
    String getMaHopDong();
    LocalDate getNgayBanGiaoMatBang();
}
