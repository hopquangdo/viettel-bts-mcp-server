package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.cache.VolumeCacheNames;

/**
 * Volume (tongQuan/listHopDong/khuVuc) tính tiền thi công từ SanLuong nên phải mất cache mỗi
 * khi SanLuong đổi — dùng lại đúng event mà sanluong đã phát (SanLuongCacheInvalidationConsumer),
 * không tạo event riêng. Đây là ví dụ cho "kiến trúc cache chung": 1 event nguồn (sanluong.changed),
 * nhiều consumer độc lập theo module tiêu thụ để tự invalidate cache của module mình.
 *
 * Cache key của loadBatch() gộp theo (loaiHopDongId, hopDongId, search) nên không xác định
 * chính xác entry nào bị ảnh hưởng bởi 1 hopDongId cụ thể — evict toàn bộ cache BATCH, chấp
 * nhận được vì TTL đã ngắn (mặc định 30s).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VolumeConsumer {

    private final CacheService cacheService;

    @KafkaListener(topics = "${app.kafka-topics.sanluong-changed}", groupId = "bts-ops-platform-volume-cache")
    public void onSanLuongChanged(SanLuongChangedEvent event) {
        log.debug("evicting volume batch cache due to sanluong change hopDongId={}", event.hopDongId());
        cacheService.evictAll(VolumeCacheNames.BATCH);
    }
}
