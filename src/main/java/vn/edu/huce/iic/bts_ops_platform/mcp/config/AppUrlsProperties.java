package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.urls")
public record AppUrlsProperties(
        String frontendBase,
        String apiPublicBase,
        List<String> corsAllowedOrigins
) {}
