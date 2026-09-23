package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.AppEventsProperties;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventEnvelope;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventTransport;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAppEventTransport implements AppEventTransport {

    private final AppEventsProperties properties;

    @Override
    public String name() {
        return "kafka";
    }

    @Override
    public void publish(AppEventEnvelope envelope) {
        if (!properties.kafkaEnabled()) {
            log.debug(
                    "kafka transport skipped (disabled) eventId={} type={}",
                    envelope.event().eventId(),
                    envelope.event().type());
            return;
        }

        String topic = properties.kafka() == null ? "app-events" : properties.kafka().topic();
        log.info(
                "kafka transport pending broker integration topic={} eventId={} category={} type={}",
                topic,
                envelope.event().eventId(),
                envelope.event().category(),
                envelope.event().type());
        // Extension point: inject KafkaTemplate and send serialized envelope JSON.
    }
}
