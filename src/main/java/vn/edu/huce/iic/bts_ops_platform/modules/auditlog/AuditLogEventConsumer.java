package vn.edu.huce.iic.bts_ops_platform.modules.auditlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventCategory;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventEnvelope;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventMetadata;
import vn.edu.huce.iic.bts_ops_platform.modules.auditlog.entity.AuditLog;
import vn.edu.huce.iic.bts_ops_platform.modules.auditlog.repository.AuditLogRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Người ghi audit_log — nghe AppEventEnvelope (transport internal) và persist các event
 * category AUDIT. Chạy async + transaction riêng, nuốt mọi lỗi: ghi audit không bao giờ được
 * làm hỏng/chậm nghiệp vụ gốc. Event có "doiTuongIds" thì ghi 1 dòng cho TỪNG trạm để màn
 * "Lịch sử chỉnh sửa trạm" truy theo doi_tuong_id.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventConsumer {

    private final AuditLogRepository auditLogRepository;

    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAppEvent(AppEventEnvelope envelope) {
        try {
            if (envelope == null || envelope.event() == null
                    || envelope.event().category() != AppEventCategory.AUDIT) {
                return;
            }
            Map<String, Object> attrs = envelope.event().attributes();
            AppEventMetadata metadata = envelope.metadata();

            UUID actorId = metadata != null && metadata.actorId() != null
                    ? metadata.actorId()
                    : parseUuid(attrs.get("actorId"));
            String actorName = metadata != null && hasText(metadata.actorName())
                    ? metadata.actorName()
                    : firstText(attrs.get("actorName"), attrs.get("tenDangNhap"));
            String ip = metadata != null ? metadata.clientIp() : null;
            UUID hopDongId = parseUuid(attrs.get("hopDongId"));
            List<UUID> doiTuongIds = parseUuidList(attrs.get("doiTuongIds"));

            String hanhDong = truncate(envelope.event().type(), 100);
            String moTa = buildMoTa(envelope.event().subject(), envelope.event().detail());

            List<AuditLog> rows = new ArrayList<>();
            if (doiTuongIds.isEmpty()) {
                rows.add(newRow(actorId, actorName, hanhDong, moTa, ip, null, hopDongId));
            } else {
                for (UUID doiTuongId : doiTuongIds) {
                    rows.add(newRow(actorId, actorName, hanhDong, moTa, ip, doiTuongId, hopDongId));
                }
            }
            auditLogRepository.saveAll(rows);
        } catch (Exception ex) {
            log.warn("audit log write failed type={}", envelope != null && envelope.event() != null
                    ? envelope.event().type() : null, ex);
        }
    }

    private AuditLog newRow(UUID actorId, String actorName, String hanhDong, String moTa,
                            String ip, UUID doiTuongId, UUID hopDongId) {
        AuditLog row = new AuditLog();
        row.setNguoiThucHienId(actorId);
        row.setTenNguoiThucHien(truncate(actorName, 250));
        row.setHanhDong(hanhDong);
        row.setMoTa(moTa);
        row.setIp(truncate(ip, 95));
        row.setDoiTuongId(doiTuongId);
        row.setHopDongId(hopDongId);
        return row;
    }

    // mo_ta là varchar mặc định (255) trên bảng có sẵn — cắt an toàn phía app thay vì đổi schema.
    private static String buildMoTa(String subject, String detail) {
        String base = subject == null ? "" : subject.trim();
        if (hasText(detail)) {
            base = base.isBlank() ? detail.trim() : base + " — " + detail.trim();
        }
        return truncate(base, 250);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String firstText(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate != null && hasText(String.valueOf(candidate))) {
                return String.valueOf(candidate);
            }
        }
        return null;
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static UUID parseUuid(Object value) {
        if (value == null) return null;
        try {
            return value instanceof UUID uuid ? uuid : UUID.fromString(String.valueOf(value).trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static List<UUID> parseUuidList(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<UUID> ids = new ArrayList<>();
        for (Object item : collection) {
            UUID id = parseUuid(item);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
