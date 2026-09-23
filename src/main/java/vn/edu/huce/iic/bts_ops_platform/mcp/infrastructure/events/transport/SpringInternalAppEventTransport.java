package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventEnvelope;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventTransport;

@Component
@RequiredArgsConstructor
public class SpringInternalAppEventTransport implements AppEventTransport {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public String name() {
        return "internal";
    }

    @Override
    public void publish(AppEventEnvelope envelope) {
        applicationEventPublisher.publishEvent(envelope);
    }
}
