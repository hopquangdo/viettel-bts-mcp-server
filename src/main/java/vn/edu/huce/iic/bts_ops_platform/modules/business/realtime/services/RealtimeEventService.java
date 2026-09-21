package vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface RealtimeEventService {

    SseEmitter subscribe(UUID userId);

    void pushNotification(UUID userId, UUID thongBaoId, String loai);

    void broadcastDashboardRefresh();
}
