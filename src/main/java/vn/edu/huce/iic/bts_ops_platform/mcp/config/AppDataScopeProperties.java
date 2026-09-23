package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.data-scope")
public record AppDataScopeProperties(boolean enabled) {}