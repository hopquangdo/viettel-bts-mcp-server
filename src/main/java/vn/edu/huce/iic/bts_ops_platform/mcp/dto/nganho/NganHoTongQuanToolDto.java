package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Tổng quan đối soát ngân sách toàn hệ thống/theo bộ lọc — mirror VolumeTongQuanResponse (module Volume), tự chứa để tools không phụ thuộc DTO của module khác. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoTongQuanToolDto {

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
    private BigDecimal tongQuyetToan;
    private long soHopDongDaQuyetToan;
    private long soHopDongDangQuyetToan;
    private long soHopDongChuaQuyetToan;
    private long tongTram;
    private long soHopDongDaThiCong;
    private long tongTramDaThiCong;
    private long soTramBatThuong;
    private long soHopDongThieuLon;
    private long tongTramDaQuyetToan;
    private long tongTramDangQuyetToan;
    private long tongTramChuaQuyetToan;
    private long giaTriDaQuyetToan;
    private long giaTriDangQuyetToan;
    private long giaTriChuaQuyetToan;
    @Builder.Default
    private Map<UUID, Long> soHopDongTheoLoai = new LinkedHashMap<>();
}
