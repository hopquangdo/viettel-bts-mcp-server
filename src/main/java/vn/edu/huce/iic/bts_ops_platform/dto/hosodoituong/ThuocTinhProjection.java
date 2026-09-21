package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import java.time.LocalDate;

public interface ThuocTinhProjection {
    String getMaDoiTuong();
    String getTenDoiTuong();
    String getMaHopDong();
    String getTenHopDong();
    LocalDate getNgayBanGiaoMatBang();
    String getTrangThaiVatTuA();
    String getTrangThaiVatTuB();
    LocalDate getNgayYeuCauVatTuB();
    LocalDate getNgayHoanThanhVatTuB();
}
