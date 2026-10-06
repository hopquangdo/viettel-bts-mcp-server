package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;

import java.math.BigDecimal;
import java.util.Map;

/**
 * volume_tong_quan: đối soát ngân sách hợp đồng với thành tiền thi công; tiền đã định dạng ("1,25 tỷ đồng"), tỷ lệ "82,5%".
 *
 * @param soHopDongTheoLoai số hợp đồng theo loại hợp đồng
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NganHoTongQuanToolResponse(
        long soHopDong,
        String giaTriHopDong,
        String tongThanhTienThiCong,
        String tongKpi,
        String chenhLech,
        String tyLeSuDung,
        long soHopDongVuotNguong,
        long soHopDongCanhBao,
        long soHopDongThieu,
        long soHopDongThieuLon,
        long soHopDongThua,
        long soHopDongCanBang,
        long soHopDongDaThiCong,
        long tongTram,
        long tongTramDaThiCong,
        long soTramBatThuong,
        String tongQuyetToan,
        long soHopDongDaQuyetToan,
        long soHopDongDangQuyetToan,
        long soHopDongChuaQuyetToan,
        long tongTramDaQuyetToan,
        long tongTramDangQuyetToan,
        long tongTramChuaQuyetToan,
        String giaTriDaQuyetToan,
        String giaTriDangQuyetToan,
        String giaTriChuaQuyetToan,
        Map<String, Long> soHopDongTheoLoai) {

    public static NganHoTongQuanToolResponse of(NganHoTongQuanToolDto t) {
        return new NganHoTongQuanToolResponse(
                t.getSoHopDong(),
                tien(t.getGiaTriHopDong()),
                McpFormat.tien(t.getTongThanhTienThiCong()),
                McpFormat.tien(t.getTongKpi()),
                McpFormat.tien(t.getChenhLech()),
                McpFormat.phanTram(t.getTyLeSuDung()),
                t.getSoHopDongVuotNguong(),
                t.getSoHopDongCanhBao(),
                t.getSoHopDongThieu(),
                t.getSoHopDongThieuLon(),
                t.getSoHopDongThua(),
                t.getSoHopDongCanBang(),
                t.getSoHopDongDaThiCong(),
                t.getTongTram(),
                t.getTongTramDaThiCong(),
                t.getSoTramBatThuong(),
                McpFormat.tien(t.getTongQuyetToan()),
                t.getSoHopDongDaQuyetToan(),
                t.getSoHopDongDangQuyetToan(),
                t.getSoHopDongChuaQuyetToan(),
                t.getTongTramDaQuyetToan(),
                t.getTongTramDangQuyetToan(),
                t.getTongTramChuaQuyetToan(),
                tien(t.getGiaTriDaQuyetToan()),
                tien(t.getGiaTriDangQuyetToan()),
                tien(t.getGiaTriChuaQuyetToan()),
                t.getSoHopDongTheoLoai());
    }

    private static String tien(Long v) {
        return v == null ? null : McpFormat.tien(BigDecimal.valueOf(v));
    }
}
