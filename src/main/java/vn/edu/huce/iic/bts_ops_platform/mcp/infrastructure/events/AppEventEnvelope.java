package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events;

public record AppEventEnvelope(AppEvent event, AppEventMetadata metadata) {}
