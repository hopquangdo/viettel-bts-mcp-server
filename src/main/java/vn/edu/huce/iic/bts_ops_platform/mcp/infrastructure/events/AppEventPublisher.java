package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events;

public interface AppEventPublisher {

    void publish(AppEventEnvelope envelope);
}
