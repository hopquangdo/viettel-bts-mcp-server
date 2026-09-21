package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Đọc hàng loạt (cache-aside): multiGet trước, tính bù (và cache lại) các id còn thiếu.
 * Trả về HopDongDoiTuongSnapshot (mã/tỉnh/khu vực/nhà thầu, tên/loại hợp đồng — nhóm tĩnh
 * do core tự tính; tổng thành tiền sản lượng, hạng mục đã làm/tổng, số vướng mắc, ngày thi
 * công gần nhất — nhóm động do business.sanluong/business.vuongmac patch vào) — dùng chung
 * bởi module volume và sanluong thay vì mỗi bên tự resolve/tính lại.
 *
 * <p>Core KHÔNG tự gọi sang business để lấy nhóm động: khi cache miss, nó chỉ cache tạm
 * bản chỉ có nhóm tĩnh rồi phát {@code SnapshotDataMissingEvent} — business.sanluong/
 * business.vuongmac tự nghe và patch phần mình sở hữu vào (xem applySanLuongFields/
 * applyVuongMacFields). Điều này giữ core không phụ thuộc ngược vào business.
 */
public interface HopDongDoiTuongSnapshotService {

    Map<UUID, HopDongDoiTuongSnapshot> getSnapshots(Collection<UUID> hopDongDoiTuongIds);

    /** Tiện ích 1 đối tượng — xem getSnapshots(Collection). */
    default HopDongDoiTuongSnapshot getSnapshot(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return null;
        }
        return getSnapshots(List.of(hopDongDoiTuongId)).get(hopDongDoiTuongId);
    }

    /**
     * Tính lại nhóm tĩnh (mã/tỉnh/khu vực/nhà thầu/tên+loại hợp đồng) cho 1 đối tượng, giữ
     * nguyên nhóm động đang cache (nếu có) — dùng bởi consumer nghe HopDongDoiTuongMetaChangedEvent
     * /HopDongMetaChangedEvent.
     */
    HopDongDoiTuongSnapshot refreshStaticFields(UUID hopDongDoiTuongId);

    /** Xóa cache snapshot — null = xóa toàn bộ (tỉnh/khu vực đổi tên); id cụ thể = xóa mọi đối tượng thuộc hợp đồng đó (tên/loại hợp đồng đổi). */
    void evictSnapshots(UUID hopDongId);

    /** Patch nhóm động thuộc sở hữu sanluong — gọi bởi business.sanluong khi SanLuongChangedEvent/SnapshotDataMissingEvent bắn tới. */
    void applySanLuongFields(
            UUID hopDongDoiTuongId,
            BigDecimal tongThanhTien,
            BigDecimal tongThanhTienSanLuong,
            int hangMucDaLam,
            int tongHangMuc,
            LocalDate constructionDate);

    /** Patch nhóm động thuộc sở hữu vuongmac — gọi bởi business.vuongmac khi VuongMacChangedEvent/SnapshotDataMissingEvent bắn tới. */
    void applyVuongMacFields(UUID hopDongDoiTuongId, int soVuongMac);
}
