package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCoVuongMacChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;

/**
 * Giữ HopDongDoiTuong.coVuongMacMo đồng bộ — nghe event nội bộ do business.vuongmac phát sau khi
 * TỰ đếm lại (repository của chính nó). Core không đọc thẳng repository của vuongmac, business
 * không gọi thẳng service ghi của core — chỉ trao đổi qua event. Ghi thẳng repository (bỏ qua
 * service) vì đây là module sở hữu HopDongDoiTuong, chỉ update 1 cột đơn giản.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongDoiTuongVuongMacConsumer {

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCoVuongMacChanged(HopDongDoiTuongCoVuongMacChangedEvent event) {
        if (event.hopDongDoiTuongId() == null) return;
        try {
            hopDongDoiTuongRepository.updateCoVuongMacMo(event.hopDongDoiTuongId(), event.coVuongMacMo());
        } catch (Exception ex) {
            log.warn("failed to update co_vuong_mac_mo for hopDongDoiTuongId={}", event.hopDongDoiTuongId(), ex);
        }
    }
}
