package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucCongViecService;

/**
 * Cleans up hạng mục records that belonged to a deleted hợp đồng. Replaces the
 * direct HangMucCongViecService call hopdong used to perform itself.
 */
@Slf4j
@Component("hangMucHopDongDeletedConsumer")
@RequiredArgsConstructor
public class HangMucConsumer {

    private final HangMucCongViecService hangMucCongViecService;

    @KafkaListener(topics = "${app.kafka-topics.hop-dong-deleted}", groupId = "bts-ops-platform-hangmuc-hopdong-deleted")
    @Transactional
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        int count = hangMucCongViecService.deleteAllHangMucByHopDongId(event.hopDongId());
        log.debug("hangmuc cleaned up {} records after hopDongId={} deletion", count, event.hopDongId());
    }
}
