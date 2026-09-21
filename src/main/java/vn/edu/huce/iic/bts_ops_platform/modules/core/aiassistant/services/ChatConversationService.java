package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ChatHistoryMessage;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationCreateResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.dto.ConversationSummary;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity.ChatConversation;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity.ChatMessage;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.exception.AiAssistantErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.repository.ChatConversationRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.repository.ChatMessageRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Conversation and message persistence. The backend is the single owner of chat history. */
@Service
@RequiredArgsConstructor
public class ChatConversationService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    @Transactional
    public ConversationCreateResponse create(UUID userId) {
        ChatConversation conversation = conversationRepository.save(new ChatConversation(UUID.randomUUID(), userId));
        return new ConversationCreateResponse(conversation.getId().toString(), conversation.getCreatedAt());
    }

    /**
     * Returns the conversation for a new chat turn: creates it when {@code sessionId} is blank or
     * unknown, and rejects a session that belongs to another user.
     */
    @Transactional
    public UUID claim(UUID userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return conversationRepository.save(new ChatConversation(UUID.randomUUID(), userId)).getId();
        }
        UUID id = parseId(sessionId);
        return conversationRepository.findById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw new AppException(AiAssistantErrorCode.CONVERSATION_FORBIDDEN,
                                "This conversation does not belong to you");
                    }
                    return existing.getId();
                })
                .orElseGet(() -> conversationRepository.save(new ChatConversation(id, userId)).getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ConversationSummary> list(UUID userId, int page, int limit) {
        Page<ChatConversation> result = conversationRepository.findActiveByUserId(userId, PageRequest.of(page - 1, limit));
        int safePage = clampPage(page, result.getTotalPages());
        if (safePage != page) {
            result = conversationRepository.findActiveByUserId(userId, PageRequest.of(safePage - 1, limit));
        }

        List<UUID> ids = result.getContent().stream().map(ChatConversation::getId).toList();
        Map<UUID, Long> counts = new HashMap<>();
        Map<UUID, String> titles = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : messageRepository.countByConversationIds(ids)) {
                counts.put((UUID) row[0], (Long) row[1]);
            }
            for (Object[] row : messageRepository.firstUserMessages(ids)) {
                titles.putIfAbsent((UUID) row[0], (String) row[1]);
            }
        }

        List<ConversationSummary> items = result.getContent().stream()
                .map(c -> new ConversationSummary(
                        c.getId().toString(),
                        titles.getOrDefault(c.getId(), ""),
                        counts.getOrDefault(c.getId(), 0L),
                        c.getUpdatedAt()))
                .toList();
        return PageResponse.ofItems(items, safePage, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(UUID userId, String sessionId) {
        ChatConversation conversation = requireOwned(userId, parseId(sessionId));
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conversation.getId());
        List<Map<String, Object>> charts = new ArrayList<>();
        messages.stream().filter(m -> m.getCharts() != null).forEach(m -> charts.addAll(m.getCharts()));
        return new ConversationResponse(
                conversation.getId().toString(),
                messages.stream().map(ChatConversationService::toHistory).toList(),
                charts,
                conversation.getUpdatedAt());
    }

    /**
     * One page of messages. With {@code descending} the first page holds the newest messages, which
     * suits "scroll up to load more"; {@code items} are always returned oldest first.
     */
    @Transactional(readOnly = true)
    public PageResponse<ChatHistoryMessage> messages(UUID userId, String sessionId, int page, int limit, boolean descending) {
        UUID id = requireOwned(userId, parseId(sessionId)).getId();
        Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "createdAt").and(Sort.by(direction, "id"));

        Page<ChatMessage> result = messageRepository.findByConversationId(id, PageRequest.of(page - 1, limit, sort));
        int safePage = clampPage(page, result.getTotalPages());
        if (safePage != page) {
            result = messageRepository.findByConversationId(id, PageRequest.of(safePage - 1, limit, sort));
        }

        List<ChatHistoryMessage> items = new ArrayList<>(result.getContent().stream().map(ChatConversationService::toHistory).toList());
        if (descending) {
            Collections.reverse(items);
        }
        return PageResponse.ofItems(items, safePage, limit, result.getTotalElements());
    }

    @Transactional
    public void saveUserMessage(UUID conversationId, String content) {
        messageRepository.save(new ChatMessage(conversationId, ChatMessage.ROLE_USER, content));
        conversationRepository.touch(conversationId, Instant.now());
    }

    @Transactional
    public void saveAssistantMessage(UUID conversationId, String content, List<Map<String, Object>> charts) {
        messageRepository.save(new ChatMessage(conversationId, ChatMessage.ROLE_ASSISTANT, content, charts));
        conversationRepository.touch(conversationId, Instant.now());
    }

    /** Missing and foreign conversations look the same, so ids of other users are not revealed. */
    private ChatConversation requireOwned(UUID userId, UUID id) {
        return conversationRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(AiAssistantErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found"));
    }

    private static int clampPage(int page, int totalPages) {
        return Math.max(1, Math.min(page, totalPages));
    }

    private static UUID parseId(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new AppException(AiAssistantErrorCode.INVALID_SESSION_ID, "Invalid session id");
        }
    }

    private static ChatHistoryMessage toHistory(ChatMessage message) {
        return new ChatHistoryMessage(message.getRole(), message.getContent());
    }
}
