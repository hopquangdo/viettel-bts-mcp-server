package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of {@code POST /chat/stream}. {@code sessionId} is optional: null starts a new conversation. */
public record ChatStreamRequest(
        @NotBlank @Size(max = 8000) String message,
        String sessionId,
        String model
) {}
