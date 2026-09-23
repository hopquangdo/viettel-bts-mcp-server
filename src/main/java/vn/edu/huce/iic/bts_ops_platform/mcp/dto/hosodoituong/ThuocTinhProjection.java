package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong;

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
    /** Ngày ghi nhận sản lượng "done" đầu tiên — null nghĩa là chưa khởi công. */
    LocalDate getNgayKhoiCong();
}
