package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCascadeDeleteEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.VuongMacChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.cache.HopDongAggregateCache;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache.HopDongDoiTuongReportCacheNames;

/**
 * HopDongService.thongKe() (hopdong_tongquan) đọc tiến độ/vướng mắc của TỪNG đối tượng thuộc
 * MỌI hợp đồng, nên không thể evict theo 1 khóa hẹp như cache row — bất kỳ thay đổi nào ở đây
 * đều evictAll(). Các thay đổi tự thân hợp đồng (create/update/delete/cancel) đã evict trực
 * tiếp trong HopDongServiceImpl; listener này phủ nốt phần thay đổi đến từ module khác.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongAggregateCacheInvalidationListener {

    private final HopDongAggregateCache hopDongAggregateCache;
    private final CacheService cacheService;

    /** Trạng thái thi công/tiến độ của 1 đối tượng đổi — ảnh hưởng tienDoTheoTrangThai, tyLeHoanThanh, soHopDongChamTienDo,
     * breakdownTheoLoaiKhuVuc, thieuCapNhatTienDo, tocDoHoanThanh. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongMetaChanged(HopDongDoiTuongMetaChangedEvent event) {
        hopDongAggregateCache.evictAll();
        cacheService.evictAll(HopDongDoiTuongReportCacheNames.REPORT);
    }

    /** Vướng mắc mở/đóng — ảnh hưởng soHopDongVuongMac, breakdownTheoLoaiKhuVuc. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onVuongMacChanged(VuongMacChangedEvent event) {
        hopDongAggregateCache.evictAll();
        cacheService.evictAll(HopDongDoiTuongReportCacheNames.REPORT);
    }

    /** Sản lượng đổi — ảnh hưởng sanLuongBatThuong, thieuCapNhatTienDo. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSanLuongChanged(SanLuongChangedEvent event) {
        cacheService.evictAll(HopDongDoiTuongReportCacheNames.REPORT);
    }

    /** Xóa hàng loạt đối tượng — ảnh hưởng mọi con số đếm theo hợp đồng. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongCascadeDelete(HopDongDoiTuongCascadeDeleteEvent event) {
        hopDongAggregateCache.evictAll();
        cacheService.evictAll(HopDongDoiTuongReportCacheNames.REPORT);
    }
}
