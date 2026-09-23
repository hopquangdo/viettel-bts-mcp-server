package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events;

public interface AppEventTransport {

    String name();

    void publish(AppEventEnvelope envelope);
}
