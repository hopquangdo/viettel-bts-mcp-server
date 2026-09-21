package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cache in-memory ngắn hạn cho số liệu tổng hợp hợp đồng (HopDongService.thongKe) — tránh
 * quét lại toàn bộ hợp đồng/đối tượng mỗi lần dashboard hoặc chatbot hỏi tổng quan.
 * Cùng pattern với TramTonAggregateCache.
 */
@Component
public class HopDongAggregateCache {

    /**
     * TTL dài vì cache được refresh/evict chủ động qua event (HopDongMetaChangedEvent,
     * HopDongDoiTuongMetaChangedEvent, VuongMacChangedEvent...) thay vì phụ thuộc TTL hết hạn —
     * xem HopDongAggregateCacheInvalidationListener. Cùng convention với vuongMacRowTtl/sanluongTongHopTtl.
     */
    public static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private record Entry<T>(T value, Instant expiresAt) {
        boolean alive() {
            return expiresAt.isAfter(Instant.now());
        }
    }

    private final ConcurrentHashMap<String, Entry<?>> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Entry<?> entry = store.get(key);
        if (entry == null || !entry.alive()) {
            if (entry != null) {
                store.remove(key, entry);
            }
            return Optional.empty();
        }
        Object value = entry.value();
        if (value == null || !type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of((T) value);
    }

    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        return get(key, type).orElseGet(() -> {
            synchronized (lockFor(key)) {
                return get(key, type).orElseGet(() -> {
                    T loaded = loader.get();
                    put(key, loaded, DEFAULT_TTL);
                    return loaded;
                });
            }
        });
    }

    public void put(String key, Object value, Duration ttl) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        Duration effectiveTtl = ttl == null || ttl.isZero() || ttl.isNegative() ? DEFAULT_TTL : ttl;
        store.put(key, new Entry<>(value, Instant.now().plus(effectiveTtl)));
    }

    public void evictAll() {
        store.clear();
    }

    public void evict(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        store.remove(key);
    }

    private Object lockFor(String key) {
        return locks.computeIfAbsent(key, k -> new Object());
    }
}
