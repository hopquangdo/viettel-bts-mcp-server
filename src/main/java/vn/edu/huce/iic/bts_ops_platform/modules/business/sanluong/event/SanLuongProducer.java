package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.config.AppKafkaTopicsProperties;

/**
 * Forwards SanLuongChangedEvent to Kafka only after the originating DB transaction
 * commits, so consumers never race a re-read against an in-flight write.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SanLuongProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final AppKafkaTopicsProperties topicsProperties;

    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSanLuongChanged(SanLuongChangedEvent event) {
        String key = event.hopDongId() != null ? event.hopDongId().toString() : "unknown";
        kafkaTemplate.send(topicsProperties.sanluongChanged(), key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("failed to publish SanLuongChangedEvent hopDongId={}", event.hopDongId(), ex);
                    }
                });
    }
}
