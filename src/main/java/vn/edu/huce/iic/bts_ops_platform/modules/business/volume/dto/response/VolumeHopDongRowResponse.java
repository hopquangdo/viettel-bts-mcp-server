package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeHopDongRowResponse {

    private UUID hopDongId;
    private String maHopDong;
    private String ma;
    private String ten;
    private UUID loaiHopDongId;
    private String loaiTen;
    private UUID kieuHopDongId;
    /** Tên đối tượng quản lý của HĐ (Trạm, Tuyến, ...) để hiển thị linh hoạt. */
    private String doiTuongTen;
    /** Chủ đầu tư — lấy từ thuộc tính động của HĐ (khớp tên "chủ đầu tư"), "—" nếu chưa cấu hình. */
    private String chuDauTu;
    private Long giaTriHd;
    private BigDecimal tongThanhTienThiCong;
    private BigDecimal chenhLech;
    private BigDecimal tyLeSuDung;
    /** binh_thuong | canh_bao | vuot_nguong */
    private String trangThai;

    private long soTram;
    private BigDecimal giaTriBinhQuan;
    private long tramThieu;
    private long tramThua;
    private long tramBatThuong;
    /** thieu | thua | can_bang */
    private String trangThaiVolume;
    /** danger | warning | ok */
    private String alertLevel;
    private String alertText;
    private BigDecimal heSoNguong;
    /** Tổng quyết toán thực — cộng dồn từ các trạm đã nhập, không suy diễn. */
    private BigDecimal tongQuyetToan;
    /** da_qt (tất cả trạm đã nhập) | dang_qt (một phần) | chua_qt (chưa trạm nào) */
    private String trangThaiQt;
}
