package vn.edu.huce.iic.bts_ops_platform.infrastructure.events;

public record AppEventEnvelope(AppEvent event, AppEventMetadata metadata) {}
