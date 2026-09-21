package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity.ChatConversation;

import java.time.Instant;
import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    /** Only conversations with at least one message, most recently updated first. */
    @Query(value = """
            select c from ChatConversation c
            where c.userId = :userId
              and exists (select 1 from ChatMessage m where m.conversationId = c.id)
            order by c.updatedAt desc
            """,
            countQuery = """
                    select count(c) from ChatConversation c
                    where c.userId = :userId
                      and exists (select 1 from ChatMessage m where m.conversationId = c.id)
                    """)
    Page<ChatConversation> findActiveByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query("update ChatConversation c set c.updatedAt = :now where c.id = :id")
    void touch(@Param("id") UUID id, @Param("now") Instant now);
}
