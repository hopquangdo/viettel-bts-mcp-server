package vn.edu.huce.iic.bts_ops_platform.infrastructure.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AppEvent(
        UUID eventId,
        AppEventCategory category,
        String type,
        String subject,
        String detail,
        Map<String, Object> attributes,
        Instant occurredAt) {

    public AppEvent {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static AppEvent audit(String action, String label, String detail) {
        return new AppEvent(
                UUID.randomUUID(),
                AppEventCategory.AUDIT,
                action,
                label,
                detail,
                Map.of(),
                Instant.now());
    }

    public static AppEvent domain(String type, String subject, String detail, Map<String, Object> attributes) {
        return new AppEvent(
                UUID.randomUUID(),
                AppEventCategory.DOMAIN,
                type,
                subject,
                detail,
                attributes,
                Instant.now());
    }
}
