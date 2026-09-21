package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.client.ChatbotClient;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatHistoryMessage;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatStreamRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatStreamResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationCreateResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationSummary;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.services.ChatConversationService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.services.ChatStreamService;

import java.util.UUID;

/**
 * AI assistant API. The backend is the only entry point for the frontend: it authenticates the user,
 * owns conversation history and relays chat streams to the (internal) chatbot service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-assistant")
@Tag(name = "AI Assistant", description = "Chat with the AI assistant and manage conversation history")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class AiAssistantController {

    private static final int DEFAULT_CONVERSATION_LIMIT = 50;
    private static final int MAX_CONVERSATION_LIMIT = 200;
    private static final int DEFAULT_MESSAGE_LIMIT = 20;
    private static final int MAX_MESSAGE_LIMIT = 100;

    private final ChatConversationService conversationService;
    private final ChatStreamService streamService;
    private final ChatbotClient chatbotClient;

    @PostMapping("/chat/conversations")
    @Operation(summary = "Create an empty conversation")
    public ResponseEntity<ApiResponse<ConversationCreateResponse>> createConversation() {
        return ApiResponse.<ConversationCreateResponse>build()
                .withData(conversationService.create(currentUserId()))
                .toEntity();
    }

    @GetMapping("/chat/conversations")
    @Operation(summary = "List the current user's conversations, newest first")
    public ResponseEntity<ApiResponse<PageResponse<ConversationSummary>>> listConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + DEFAULT_CONVERSATION_LIMIT) int limit) {
        return ApiResponse.<PageResponse<ConversationSummary>>build()
                .withData(conversationService.list(currentUserId(), atLeastOne(page), clamp(limit, MAX_CONVERSATION_LIMIT)))
                .toEntity();
    }

    @GetMapping("/chat/conversation/{sessionId}")
    @Operation(summary = "Get a conversation with all its messages and charts")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable String sessionId) {
        return ApiResponse.<ConversationResponse>build()
                .withData(conversationService.get(currentUserId(), sessionId))
                .toEntity();
    }

    @GetMapping("/chat/messages/{sessionId}")
    @Operation(
            summary = "Get a page of messages",
            description = "`order=desc` makes page 1 the newest messages (scroll-up loading); items are always oldest first.")
    public ResponseEntity<ApiResponse<PageResponse<ChatHistoryMessage>>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + DEFAULT_MESSAGE_LIMIT) int limit,
            @RequestParam(defaultValue = "asc") String order) {
        return ApiResponse.<PageResponse<ChatHistoryMessage>>build()
                .withData(conversationService.messages(
                        currentUserId(), sessionId, atLeastOne(page), clamp(limit, MAX_MESSAGE_LIMIT),
                        "desc".equalsIgnoreCase(order)))
                .toEntity();
    }

    @PostMapping("/chat/stream")
    @Operation(
            summary = "Step 1/2: start a chat turn",
            description = "Saves the user message, starts the agent and returns `streamId` and `sessionId`. "
                    + "Omit `sessionId` to start a new conversation.")
    public ResponseEntity<ApiResponse<ChatStreamResponse>> createStream(@Valid @RequestBody ChatStreamRequest request) {
        return ApiResponse.<ChatStreamResponse>build()
                .withData(streamService.start(SecurityContextHelper.requireCurrentUser(), request))
                .toEntity();
    }

    @GetMapping(value = "/chat/stream/{streamId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Step 2/2: receive the answer as Server-Sent Events",
            description = "A stream can be opened once. The assistant message is saved when the run finishes.")
    public ResponseEntity<StreamingResponseBody> openStream(@PathVariable String streamId, HttpServletResponse response) {
        StreamingResponseBody body = streamService.open(currentUserId(), streamId, response);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                // no-transform: proxy trung gian (Next rewrite/Nginx) không được nén/gom buffer SSE.
                .cacheControl(CacheControl.noStore().noTransform())
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    @GetMapping("/models")
    @Operation(summary = "List models available for selection")
    public ResponseEntity<ApiResponse<Object>> listModels() {
        return ApiResponse.<Object>build()
                .withData(chatbotClient.models(currentUserId()))
                .toEntity();
    }

    private static UUID currentUserId() {
        return SecurityContextHelper.requireCurrentUser().id();
    }

    private static int atLeastOne(int value) {
        return Math.max(1, value);
    }

    private static int clamp(int value, int max) {
        return Math.max(1, Math.min(value, max));
    }
}
