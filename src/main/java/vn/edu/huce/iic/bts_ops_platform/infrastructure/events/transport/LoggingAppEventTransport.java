package vn.edu.huce.iic.bts_ops_platform.infrastructure.events.transport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventEnvelope;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventTransport;

@Slf4j
@Component
public class LoggingAppEventTransport implements AppEventTransport {

    @Override
    public String name() {
        return "logging";
    }

    @Override
    public void publish(AppEventEnvelope envelope) {
        var event = envelope.event();
        var metadata = envelope.metadata();
        log.info(
                "app-event category={} type={} subject={} eventId={} actorId={} actorName={} requestId={} clientIp={} source={} detail={}",
                event.category(),
                event.type(),
                event.subject(),
                event.eventId(),
                metadata.actorId(),
                metadata.actorName(),
                metadata.requestId(),
                metadata.clientIp(),
                metadata.source(),
                event.detail());
    }
}
