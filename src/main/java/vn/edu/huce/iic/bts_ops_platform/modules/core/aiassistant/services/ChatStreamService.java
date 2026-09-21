package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.config.AppChatbotProperties;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtService;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.client.ChatbotClient;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatStreamRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatStreamResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.exception.AiAssistantErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Two-step chat streaming, kept compatible with the frontend flow:
 * <ol>
 *   <li>{@link #start} checks conversation ownership, starts the agent run in the chatbot, saves the
 *       user message and remembers which conversation/user the returned stream id belongs to;</li>
 *   <li>{@link #open} relays the chatbot's SSE frames to the client and, once the run has finished,
 *       saves the assistant answer (from the {@code done} frame) together with its charts.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatStreamService {

    private static final String KEY_PREFIX = "ai-assistant:stream:";

    private final ChatbotClient chatbot;
    private final ChatConversationService conversations;
    private final StringRedisTemplate redis;
    private final AppChatbotProperties properties;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public ChatStreamResponse start(JwtUserPrincipal user, ChatStreamRequest request) {
        UUID userId = user.id();
        UUID conversationId = conversations.claim(userId, request.sessionId());
        // The chatbot acts on behalf of this user towards MCP with a short-lived, MCP-only token.
        String streamId = chatbot.createStream(
                userId, jwtService.createMcpToken(user), conversationId, request.message(), request.model());
        conversations.saveUserMessage(conversationId, request.message());
        redis.opsForValue().set(KEY_PREFIX + streamId, userId + ":" + conversationId, properties.streamTtl());
        return new ChatStreamResponse(streamId, conversationId.toString());
    }

    /**
     * Validates the stream and opens the upstream connection eagerly, so failures surface as proper
     * HTTP errors; the returned body relays frames and persists the answer.
     */
    public StreamingResponseBody open(UUID userId, String streamId, HttpServletResponse response) {
        String key = KEY_PREFIX + streamId;
        String owner = redis.opsForValue().get(key);
        String[] parts = owner == null ? new String[0] : owner.split(":");
        // Unknown and foreign streams are indistinguishable on purpose.
        if (parts.length != 2 || !parts[0].equals(userId.toString())) {
            throw new AppException(AiAssistantErrorCode.STREAM_NOT_FOUND, "Stream not found or already consumed");
        }
        UUID conversationId = UUID.fromString(parts[1]);
        redis.delete(key);

        InputStream upstream = chatbot.openStream(userId, streamId);
        return out -> relay(upstream, conversationId, out, response);
    }

    private void relay(InputStream upstream, UUID conversationId, OutputStream out, HttpServletResponse response) {
        boolean clientOpen = true;
        String reply = null;
        List<Map<String, Object>> charts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(upstream, StandardCharsets.UTF_8))) {
            StringBuilder frame = new StringBuilder();
            StringBuilder data = new StringBuilder();
            String event = "message";
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    frame.append(line).append('\n');
                    if (line.startsWith("event:")) {
                        event = line.substring(6).trim();
                    } else if (line.startsWith("data:")) {
                        data.append(line.substring(5).trim());
                    }
                    continue;
                }
                if (frame.isEmpty()) {
                    continue;
                }
                frame.append('\n');
                // Keep draining after the client is gone so the answer is still saved.
                if (clientOpen) {
                    clientOpen = write(out, response, frame.toString());
                }
                try {
                    if ("done".equals(event)) {
                        reply = objectMapper.readTree(data.toString()).path("reply").asText("");
                    } else if ("chart".equals(event)) {
                        charts.add(objectMapper.convertValue(objectMapper.readTree(data.toString()),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}));
                    }
                } catch (IOException | IllegalArgumentException e) {
                    log.warn("Skipping malformed '{}' frame from chatbot: {}", event, e.getMessage());
                }
                frame.setLength(0);
                data.setLength(0);
                event = "message";
            }
        } catch (IOException e) {
            log.warn("Chatbot stream for conversation {} ended abnormally: {}", conversationId, e.getMessage());
        }

        if (reply != null) {
            try {
                conversations.saveAssistantMessage(conversationId, reply, charts);
            } catch (RuntimeException e) {
                log.error("Failed to save assistant message for conversation {}", conversationId, e);
            }
        } else {
            log.warn("Conversation {} finished without a 'done' frame; no assistant message saved", conversationId);
        }
    }

    /**
     * Spring 7's ServletServerHttpResponse hands StreamingResponseBody a non-flushing stream unless
     * {@code spring.http.response.flush.enabled} is set, so {@code out.flush()} is a no-op and Tomcat
     * holds the frames until its 8 KB buffer fills. flushBuffer() on the servlet response pushes each
     * frame to the client immediately.
     */
    private boolean write(OutputStream out, HttpServletResponse response, String frame) {
        try {
            out.write(frame.getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
            return true;
        } catch (IOException e) {
            log.debug("Client disconnected: {}", e.getMessage());
            return false;
        }
    }
}
