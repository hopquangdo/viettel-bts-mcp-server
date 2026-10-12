package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;

import java.util.List;

/**
 * doituongton_tong_quan: số liệu tổng hợp đối tượng tồn, tiền đã định dạng ("1,25 tỷ đồng").
 *
 * @param soDuDieuKien   đủ điều kiện ghi doanh thu (chờ quyết toán + quá hạn)
 * @param nguongQuaHan   "90 ngày"
 * @param theoTuoiTon    phân bố đối tượng đủ điều kiện theo tuổi tồn
 * @param theoLyDo       phân bố đối tượng chưa đủ điều kiện theo lý do
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TramTonTongQuanToolResponse(
        long tongSoDoiTuongTon,
        long soDuDieuKien,
        long choQuyetToan,
        long quaHan,
        long chuaDuDieuKien,
        String nguongQuaHan,
        String giaTriTon,
        List<Nhom> theoTuoiTon,
        List<Nhom> theoLyDo) {

    /** 1 nhóm phân bố: tên nhóm, số đối tượng, giá trị, tỷ lệ trên tổng của nhóm cha ("12,5%"). */
    public record Nhom(String nhom, long soLuong, String giaTri, String tyLe) {
    }

    public static TramTonTongQuanToolResponse of(TramTonToolTongQuanDto tq) {
        return new TramTonTongQuanToolResponse(tq.getTongSoTram(), tq.getDuDieuKienDt(), tq.getChoQuyetToan(), tq.getQuaHan(),
                tq.getChuaDuDieuKien(), tq.getQuaHanNgay() + " ngày", McpFormat.tien(tq.getGiaTriTon()),
                nhom(tq.getAging()), nhom(tq.getReasons()));
    }

    private static List<Nhom> nhom(List<TramTonToolBucketDto> buckets) {
        return buckets == null ? null : buckets.stream()
                .map(b -> new Nhom(b.getLabel(), b.getCount(), McpFormat.tien(b.getValue()), McpFormat.phanTram(b.getPercent())))
                .toList();
    }
}
