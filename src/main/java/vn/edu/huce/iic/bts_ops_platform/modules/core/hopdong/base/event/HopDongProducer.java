package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.config.AppKafkaTopicsProperties;

/**
 * Forwards HopDongDeletedEvent to Kafka only after the originating DB transaction
 * commits, so consumers never race a cleanup against a rolled-back delete.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final AppKafkaTopicsProperties topicsProperties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        String key = event.hopDongId() != null ? event.hopDongId().toString() : "unknown";
        kafkaTemplate.send(topicsProperties.hopDongDeleted(), key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("failed to publish HopDongDeletedEvent hopDongId={}", event.hopDongId(), ex);
                    }
                });
    }
}