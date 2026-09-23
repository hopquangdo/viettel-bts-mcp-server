package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.logging")
public record AppLoggingProperties(RequestTrace requestTrace) {

    public record RequestTrace(boolean enabled, boolean logArgs, boolean logResult, int maxResultLength) {}
}
