package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events.helpers.AppEventMetadataResolver;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppEventContext {

    private final AppEventPublisher publisher;
    private final AppEventMetadataResolver metadataResolver;
    private final ObjectMapper objectMapper;

    public void publish(AppEvent event) {
        publish(event, null);
    }

    public void publish(AppEvent event, String source) {
        AppEventMetadata metadata = metadataResolver.resolve(source);
        publisher.publish(new AppEventEnvelope(event, metadata));
    }

    public void audit(String action, String label, String detail) {
        publish(AppEvent.audit(action, label, detail), "audit");
    }

    public void audit(String action, String label, Object detailObject) {
        audit(action, label, toJson(detailObject));
    }

    /** Audit kèm ngữ cảnh nghiệp vụ — attributes hiểu các khóa: "hopDongId" (UUID/String),
     * "doiTuongIds" (Collection UUID/String — consumer ghi 1 dòng audit_log cho TỪNG trạm),
     * "actorId"/"actorName" (fallback khi chưa có SecurityContext, vd đăng nhập). */
    public void audit(String action, String label, String detail, Map<String, Object> attributes) {
        publish(new AppEvent(
                java.util.UUID.randomUUID(),
                AppEventCategory.AUDIT,
                action,
                label,
                detail,
                attributes,
                java.time.Instant.now()), "audit");
    }

    public void domainUpdate(String type, String subject, String detail) {
        domainUpdate(type, subject, detail, Map.of());
    }

    public void domainUpdate(String type, String subject, String detail, Map<String, Object> attributes) {
        publish(AppEvent.domain(type, subject, detail, attributes), "domain");
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }
}
