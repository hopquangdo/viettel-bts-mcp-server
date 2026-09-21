package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Dòng đối tượng "chờ quyết toán"/"quá hạn" — cùng field shape với TramTonItemResponse để frontend dùng chung 1 kiểu bảng. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongDoiTuongTonItemResponse {
    private UUID id;
    private UUID hopDongId;
    private String maTram;
    private String khuVuc;
    private String nhaThau;
    private String maHopDong;
    private BigDecimal giaTri;
    /** Denormalize — xem HopDongDoiTuong.sanLuongHieuLuc. Ưu tiên hơn giaTri (fallback chia đều). */
    private BigDecimal sanLuongHieuLuc;
    private LocalDate ngayHtTc;
    private long soNgayTon;
    private String trangThai;
}
