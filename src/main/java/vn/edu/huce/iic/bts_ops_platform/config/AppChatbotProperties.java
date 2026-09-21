package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Chatbot service (internal network). {@code baseUrl} includes the API version, e.g. {@code
 * http://chatbot:8000/api/v1}. The chatbot does no authentication, so only the backend may reach it.
 */
@ConfigurationProperties(prefix = "app.chatbot")
public record AppChatbotProperties(
        @DefaultValue("http://localhost:8000/api/v1") String baseUrl,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("30s") Duration requestTimeout,
        @DefaultValue("15m") Duration streamTtl
) {}
