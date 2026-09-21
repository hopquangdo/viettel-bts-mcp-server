package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.event;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository.PhanCongRepository;

/**
 * Cleans up PhanCong records that belonged to a deleted hợp đồng. Replaces the
 * direct PhanCongRepository access hopdong used to perform itself.
 */
@Slf4j
@Component("phanCongHopDongDeletedConsumer")
@RequiredArgsConstructor
public class PhanCongConsumer {

    private final PhanCongRepository phanCongRepository;

    @KafkaListener(topics = "${app.kafka-topics.hop-dong-deleted}", groupId = "bts-ops-platform-phancong-hopdong-deleted")
    @Transactional
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        Instant now = Instant.now();
        for (PhanCong assignment : phanCongRepository.findByHopDongIdAndNgayXoaIsNull(event.hopDongId())) {
            assignment.setNgayXoa(now);
            assignment.setHoatDong(false);
            phanCongRepository.save(assignment);
        }
        log.debug("phancong cleaned up after hopDongId={} deletion", event.hopDongId());
    }
}
