package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import java.time.Instant;

/** 1 hạng mục sản lượng bị nghiệm thu KHÔNG ĐẠT, kèm lý do và người nghiệm thu. */
public interface SanLuongNghiemThuKhongDatItem {
    String getMaHangMuc();
    String getTenHangMuc();
    String getMaHopDong();
    String getLoaiDoiTuong();
    String getMaDoiTuong();
    String getNhaThau();
    String getLyDo();
    String getNguoiNghiemThu();
    Instant getNgayNghiemThu();
}
