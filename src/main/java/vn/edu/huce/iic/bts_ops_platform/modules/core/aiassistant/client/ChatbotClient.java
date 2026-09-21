package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.config.AppChatbotProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.exception.AiAssistantErrorCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for the chatbot service (internal network, no authentication). The caller's identity
 * is passed in the {@code X-User-Id} header; the backend has already authenticated the user and
 * checked conversation ownership.
 */
@Slf4j
@Component
public class ChatbotClient {

    private static final String USER_HEADER = "X-User-Id";
    /** Token the chatbot forwards to the MCP server so tools run as this user (see McpUserContext). */
    private static final String MCP_TOKEN_HEADER = "X-Mcp-Token";

    private final AppChatbotProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient http;

    public ChatbotClient(AppChatbotProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        // Ép HTTP/1.1: mặc định Java HttpClient thử nâng cấp h2c trên http:// (kèm header Upgrade/
        // HTTP2-Settings), uvicorn/h11 của chatbot không hiểu và trả 400 "Invalid HTTP request
        // received" hoặc bỏ mất body (422 "body Field required").
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.connectTimeout())
                .build();
    }

    /** Starts an agent run for one user message and returns the chatbot's stream id. */
    public String createStream(UUID userId, String mcpToken, UUID sessionId, String message, String model) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("session_id", sessionId.toString());
        if (model != null && !model.isBlank()) {
            body.put("model", model);
        }
        try {
            HttpRequest request = request("/chat/stream", userId)
                    .header(MCP_TOKEN_HEADER, mcpToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            requireSuccess(response.statusCode(), response.body());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data").isObject() ? root.path("data") : root;
            String streamId = data.path("stream_id").asText("");
            if (streamId.isEmpty()) {
                throw new AppException(AiAssistantErrorCode.CHATBOT_UNAVAILABLE, "Chatbot returned no stream id");
            }
            return streamId;
        } catch (IOException e) {
            throw unavailable(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable(e);
        }
    }

    /** Opens the SSE stream of a previously created run. The caller must close the returned body. */
    public InputStream openStream(UUID userId, String streamId) {
        try {
            HttpRequest request = request("/chat/stream/" + streamId, userId)
                    .header("Accept", "text/event-stream")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                response.body().close();
                if (response.statusCode() == 404) {
                    throw new AppException(AiAssistantErrorCode.STREAM_NOT_FOUND, "Stream not found or already consumed");
                }
                requireSuccess(response.statusCode(), body);
            }
            return response.body();
        } catch (IOException e) {
            throw unavailable(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable(e);
        }
    }

    /**
     * Models available for selection, passed through as the chatbot returns them. Trả về Map/List thuần
     * (không phải JsonNode của Jackson 2): bộ chuyển đổi HTTP của Spring Boot 4 dùng Jackson 3 và sẽ
     * tuần tự hoá JsonNode của Jackson 2 như một bean thường (ra các cờ array/textual/... thay vì nội dung).
     */
    public Object models(UUID userId) {
        try {
            HttpRequest request = request("/models", userId).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            requireSuccess(response.statusCode(), response.body());
            return objectMapper.readValue(response.body(), Object.class);
        } catch (IOException e) {
            throw unavailable(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable(e);
        }
    }

    private HttpRequest.Builder request(String path, UUID userId) {
        return HttpRequest.newBuilder(URI.create(properties.baseUrl() + path))
                .timeout(properties.requestTimeout())
                .header(USER_HEADER, userId.toString());
    }

    private void requireSuccess(int status, String body) {
        if (status >= 200 && status < 300) {
            return;
        }
        log.warn("Chatbot responded with status {}: {}", status, body);
        if (status >= 500) {
            throw new AppException(AiAssistantErrorCode.CHATBOT_UNAVAILABLE, "Chatbot is unavailable");
        }
        throw new AppException(AiAssistantErrorCode.CHATBOT_REJECTED, "Chatbot rejected the request");
    }

    private AppException unavailable(Exception cause) {
        log.error("Cannot reach chatbot at {}", properties.baseUrl(), cause);
        return new AppException(AiAssistantErrorCode.CHATBOT_UNAVAILABLE, "Chatbot is unavailable");
    }
}
