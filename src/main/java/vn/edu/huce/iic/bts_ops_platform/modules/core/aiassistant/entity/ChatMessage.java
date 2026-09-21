package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Column(name = "role", nullable = false, updatable = false, length = 16)
    private String role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Attached charts (verbatim payloads of the SSE {@code chart} event); null when the message has none. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "charts", columnDefinition = "jsonb")
    private List<Map<String, Object>> charts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ChatMessage(UUID conversationId, String role, String content) {
        this(conversationId, role, content, null);
    }

    public ChatMessage(UUID conversationId, String role, String content, List<Map<String, Object>> charts) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.charts = charts == null || charts.isEmpty() ? null : charts;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
