package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bản tổng hợp dùng chung giữa module volume và sanluong cho 1 đối tượng hợp đồng
 * (1 hopDongDoiTuongId = 1 bản ghi) — xem HopDongDoiTuongSnapshotService.getSnapshots().
 * Core chỉ tự tính nhóm tĩnh; nhóm động do business.sanluong/business.vuongmac chủ động
 * đẩy vào (xem HopDongDoiTuongSnapshotService.applySanLuongFields/applyVuongMacFields) —
 * core không bao giờ tự đi hỏi business, nên đây vẫn là dữ liệu core sở hữu (cache), dù
 * một phần nội dung do module khác điền vào.
 *
 * <ul>
 *   <li>Nhóm tĩnh (maDoiTuong/tinh/khuVuc/nhaThau/tenHopDong/loaiHopDong) — core tự tính khi
 *       cache miss hoặc khi HopDongDoiTuongMetaChangedEvent/HopDongMetaChangedEvent/
 *       GeoMetaChangedEvent bắn tới.</li>
 *   <li>Nhóm động (tongThanhTien/hangMucDaLam/tongHangMuc/soVuongMac/constructionDate) — do
 *       business.sanluong/business.vuongmac tự tính rồi patch vào, khi SanLuongChangedEvent/
 *       VuongMacChangedEvent hoặc SnapshotDataMissingEvent (core phát khi cache miss) bắn tới.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class HopDongDoiTuongSnapshot {

    private UUID hopDongDoiTuongId;
    private UUID hopDongId;

    // Nhóm tĩnh — core tự tính
    private String maDoiTuong;
    private String tinh;
    /** Mã tỉnh cũ (trước sáp nhập) — dùng gộp nhóm tỉnh + hiển thị legacyCode, xem volume. */
    private String oldProvince;
    /** id trạm khi tỉnh được suy ra từ mã trạm (không phải UUID tỉnh thật) — xem resolveObjectGeos. */
    private String provinceKey;
    private String khuVuc;
    private String nhaThau;
    /** Địa chỉ/vị trí cụ thể của đối tượng (thuộc tính EAV "địa chỉ"/"vị trí"/"điểm") — xem
     * HopDongDanhSachDoiTuongGroupSupport.resolveObjectGeo. */
    private String diaChi;
    private String tenHopDong;
    private String loaiHopDong;

    // Nhóm động — business.sanluong/business.vuongmac chủ động patch vào
    private BigDecimal tongThanhTien;
    /**
     * Tổng thành tiền sản lượng theo TẤT CẢ trạng thái (không lọc "productive" như
     * {@link #tongThanhTien}) — dùng cho volume/tramton (giá trị tồn). KHÔNG dùng thay
     * cho tongThanhTien vì khác ý nghĩa: cái này cộng cả bản ghi "issue"/"pending".
     */
    private BigDecimal tongThanhTienSanLuong;
    private int hangMucDaLam;
    private int tongHangMuc;
    private int soVuongMac;
    private LocalDate constructionDate;
}
