package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** A conversation with the AI assistant. {@code id} doubles as the chatbot's session_id / thread_id. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_conversation")
public class ChatConversation {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChatConversation(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
}
