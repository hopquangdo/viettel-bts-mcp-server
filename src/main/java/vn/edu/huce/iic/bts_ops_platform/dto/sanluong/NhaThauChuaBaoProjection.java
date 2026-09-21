package vn.edu.huce.iic.bts_ops_platform.dto.sanluong;

import java.util.UUID;

public interface NhaThauChuaBaoProjection {
    UUID getNhaThauId();
    String getTenNhaThau();
    Long getSoDoiTuongPhuTrach();
    /** Tổng số nhà thầu chưa báo (cùng bộ lọc, trước khi phân trang). */
    Long getTong();
}
