package vn.edu.huce.iic.bts_ops_platform.common.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Abstraction over the underlying cache provider (Redis today; a Caffeine or
 * in-memory implementation can be swapped in without touching callers).
 */
public interface CacheService {

    <T> Optional<T> get(String cacheName, String key, Class<T> type);

    <T> java.util.List<T> multiGet(String cacheName, java.util.List<String> keys, Class<T> type);

    void put(String cacheName, String key, Object value, Duration ttl);

    void multiPut(String cacheName, java.util.Map<String, Object> values, Duration ttl);

    void evict(String cacheName, String key);

    void evictAll(String cacheName);

    /**
     * Evicts every entry whose key starts with {@code keyPrefix} within {@code cacheName}.
     * Used when a single change can affect many derived/filtered cache entries
     * (e.g. an aggregate cached per filter combination, keyed by an entity id prefix).
     */
    void evictByPrefix(String cacheName, String keyPrefix);
}
