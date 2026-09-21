package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.entity.ChatMessage;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findByConversationId(UUID conversationId, Pageable pageable);

    List<ChatMessage> findByConversationIdOrderByCreatedAtAscIdAsc(UUID conversationId);

    /** [conversationId, message count] for the conversations on a list page. */
    @Query("""
            select m.conversationId, count(m) from ChatMessage m
            where m.conversationId in :ids
            group by m.conversationId
            """)
    List<Object[]> countByConversationIds(@Param("ids") Collection<UUID> ids);

    /** [conversationId, content] of the first user message, used as the conversation title. */
    @Query("""
            select m.conversationId, m.content from ChatMessage m
            where m.conversationId in :ids and m.role = 'user'
              and m.createdAt = (select min(m2.createdAt) from ChatMessage m2
                                 where m2.conversationId = m.conversationId and m2.role = 'user')
            """)
    List<Object[]> firstUserMessages(@Param("ids") Collection<UUID> ids);
}
