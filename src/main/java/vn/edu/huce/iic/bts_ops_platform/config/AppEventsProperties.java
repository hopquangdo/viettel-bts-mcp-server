package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.events")
public record AppEventsProperties(Integration integration, Kafka kafka) {

    public record Integration(String type) {}

    public record Kafka(boolean enabled, String topic) {}

    public List<String> activeTransports() {
        String mode = integration == null || integration.type() == null
                ? "logging"
                : integration.type().trim().toLowerCase();
        return switch (mode) {
            case "internal" -> List.of("logging", "internal");
            case "kafka" -> List.of("logging", "internal", "kafka");
            case "logging" -> List.of("logging");
            default -> List.of("logging");
        };
    }

    public boolean kafkaEnabled() {
        return kafka != null && kafka.enabled();
    }
}
