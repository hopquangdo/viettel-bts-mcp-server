package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongCanceledEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCascadeDeleteEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.cache.NguonLucAggregateCache;

/**
 * NguonLucService.dangTrienKhai() (nguonluc_tongquan) đếm hợp đồng/sản lượng/đối tượng đang
 * hoạt động trên toàn hệ thống — bất kỳ thay đổi số lượng active nào ở các module này đều
 * evictAll(). Lưu ý: tạo mới (create) hiện không phát event ở các service liên quan, nên vẫn
 * còn 1 khoảng hở TTL cho trường hợp create — chấp nhận được vì cùng giới hạn với các cache
 * tongquan khác trong hệ thống (vd VuongMacService, SanLuongService).
 */
@Component
@RequiredArgsConstructor
public class NguonLucAggregateCacheInvalidationListener {

    private final NguonLucAggregateCache nguonLucAggregateCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        nguonLucAggregateCache.evictAll();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongCanceled(HopDongCanceledEvent event) {
        nguonLucAggregateCache.evictAll();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSanLuongChanged(SanLuongChangedEvent event) {
        nguonLucAggregateCache.evictAll();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongCascadeDelete(HopDongDoiTuongCascadeDeleteEvent event) {
        nguonLucAggregateCache.evictAll();
    }
}
