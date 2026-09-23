package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.AppEventsProperties;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventEnvelope;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventPublisher;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.AppEventTransport;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultAppEventPublisher implements AppEventPublisher {

    private final Map<String, AppEventTransport> transports;
    private final AppEventsProperties properties;

    public DefaultAppEventPublisher(List<AppEventTransport> transports, AppEventsProperties properties) {
        this.transports = transports.stream()
                .collect(Collectors.toMap(AppEventTransport::name, Function.identity()));
        this.properties = properties;
    }

    @Override
    public void publish(AppEventEnvelope envelope) {
        for (String transportName : properties.activeTransports()) {
            AppEventTransport transport = transports.get(transportName);
            if (transport == null) {
                log.warn("app event transport not found name={}", transportName);
                continue;
            }
            transport.publish(envelope);
        }
    }
}
