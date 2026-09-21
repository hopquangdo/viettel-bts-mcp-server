package vn.edu.huce.iic.bts_ops_platform.metrics;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Gắn bộ đếm SQL vào Hibernate. Tắt bằng {@code mcp.metrics.enabled=false}. */
@Configuration
@ConditionalOnProperty(name = "mcp.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class McpMetricsConfig {

    @Bean
    public HibernatePropertiesCustomizer mcpSqlCounterCustomizer() {
        return properties -> properties.put(AvailableSettings.STATEMENT_INSPECTOR, new McpSqlCounter());
    }
}
