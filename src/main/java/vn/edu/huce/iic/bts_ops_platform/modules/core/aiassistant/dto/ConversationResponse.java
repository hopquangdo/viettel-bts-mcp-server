package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** A whole conversation: every message plus all charts flattened in chronological order. */
public record ConversationResponse(
        String sessionId,
        List<ChatHistoryMessage> messages,
        List<Map<String, Object>> charts,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant updatedAt
) {}
