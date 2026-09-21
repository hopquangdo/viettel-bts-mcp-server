package vn.edu.huce.iic.bts_ops_platform.dto.sanluong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** 1 hạng mục lá của hợp đồng ghép với bản ghi sản lượng mới nhất của 1 đối tượng cụ thể. */
public interface HangMucDoiTuongProjection {
    UUID getDoiTuongId();
    String getMaDoiTuong();
    String getNhom();
    String getMaHangMuc();
    String getTenHangMuc();
    BigDecimal getDonGia();
    /** Trạng thái bản ghi mới nhất (done, survey, design…); null nếu chưa có bản ghi. */
    String getTrangThai();
    LocalDate getNgayThucHien();
    BigDecimal getKhoiLuong();
    BigDecimal getGiaTri();
    String getKetQuaNghiemThu();
    String getLyDoKhongDat();
}
