package vn.edu.huce.iic.bts_ops_platform.infrastructure.events;

public interface AppEventPublisher {

    void publish(AppEventEnvelope envelope);
}
