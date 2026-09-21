package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeTongQuanResponse {

    private Long giaTriHopDong;
    private BigDecimal tongThanhTienThiCong;
    private BigDecimal chenhLech;
    private BigDecimal tyLeSuDung;
    private long soHopDong;
    private long soHopDongVuotNguong;
    private long soHopDongCanhBao;
    private long soHopDongThieu;
    private long soHopDongThua;
    private long soHopDongCanBang;
    /** Tổng tiền đã quyết toán — cộng dồn quyetToanThuc thực nhập ở cấp trạm, không suy diễn. */
    private BigDecimal tongQuyetToan;
    /** HĐ có tất cả các trạm đều đã nhập quyết toán thực. */
    private long soHopDongDaQuyetToan;
    /** HĐ có một phần trạm đã nhập quyết toán thực (còn lại chưa). */
    private long soHopDongDangQuyetToan;
    /** HĐ chưa có trạm nào được nhập quyết toán thực. */
    private long soHopDongChuaQuyetToan;
    /** Tổng số trạm (mọi HĐ khớp bộ lọc) — cho dòng phụ "X HĐ · Y Trạm" ở các thẻ KPI. */
    private long tongTram;
    /** HĐ đã có sản lượng thi công thực tế > 0. */
    private long soHopDongDaThiCong;
    private long tongTramDaThiCong;
    /** Tổng số trạm có sản lượng vượt ngưỡng cảnh báo, cộng dồn mọi HĐ khớp bộ lọc. */
    private long soTramBatThuong;
    /** HĐ có (kế hoạch - thực tế) vượt quá 10% giá trị kế hoạch — thiếu sản lượng đáng kể. */
    private long soHopDongThieuLon;
    private long tongTramDaQuyetToan;
    private long tongTramDangQuyetToan;
    private long tongTramChuaQuyetToan;
    /** Tổng quyết toán thực nhập theo nhóm trạng thái QT hợp đồng (cộng tongQuyetToan từng HĐ). */
    private long giaTriDaQuyetToan;
    private long giaTriDangQuyetToan;
    private long giaTriChuaQuyetToan;
    /** Đếm số HĐ theo loaiHopDongId — phục vụ tab loại HĐ, không phụ thuộc trang của /hop-dong. */
    @Builder.Default
    private Map<UUID, Long> soHopDongTheoLoai = new LinkedHashMap<>();
}
