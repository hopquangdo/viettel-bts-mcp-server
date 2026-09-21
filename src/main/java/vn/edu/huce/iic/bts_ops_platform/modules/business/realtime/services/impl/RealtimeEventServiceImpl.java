package vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services.RealtimeEventService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class RealtimeEventServiceImpl implements RealtimeEventService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ex) {
            removeEmitter(userId, emitter);
        }
        return emitter;
    }

    @Override
    public void pushNotification(UUID userId, UUID thongBaoId, String loai) {
        sendToUser(userId, "notification", Map.of(
                "thongBaoId", thongBaoId != null ? thongBaoId.toString() : "",
                "loai", loai != null ? loai : ""));
    }

    @Override
    public void broadcastDashboardRefresh() {
        for (UUID userId : emittersByUser.keySet()) {
            sendToUser(userId, "dashboard_refresh", Map.of("at", System.currentTimeMillis()));
        }
    }

    private void sendToUser(UUID userId, String eventName, Object payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // no-op
        }
    }
}
