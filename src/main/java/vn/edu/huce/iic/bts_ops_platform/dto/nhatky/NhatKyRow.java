package vn.edu.huce.iic.bts_ops_platform.dto.nhatky;

import java.time.Instant;
import java.util.UUID;

/** 1 dòng audit_log kèm mã hợp đồng và loại đối tượng (nếu có). */
public interface NhatKyRow {
    Instant getNgay();
    String getNguoiThucHien();
    String getHanhDong();
    String getMoTa();
    String getIp();
    UUID getDoiTuongId();
    UUID getHopDongId();
    String getMaHopDong();
    String getTenHopDong();
    String getLoaiDoiTuong();
}
