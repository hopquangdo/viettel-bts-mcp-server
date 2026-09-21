package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ConversationCreateResponse(
        String sessionId,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt
) {}
