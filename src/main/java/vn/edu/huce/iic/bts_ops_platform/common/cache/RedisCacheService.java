package vn.edu.huce.iic.bts_ops_platform.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;

import java.time.Duration;
import java.util.*;

/**
 * Cache là tối ưu hoá, không phải nguồn sự thật — mọi lỗi kết nối/timeout Redis (DataAccessException,
 * bao gồm QueryTimeoutException khi Redis chậm/không phản hồi) đều bị nuốt và log warning thay vì
 * ném lên, để request chính vẫn trả dữ liệu đã tính được thay vì crash 500 vì Redis có sự cố.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppCacheProperties cacheProperties;

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        if (!cacheProperties.enabled()) {
            return Optional.empty();
        }
        String raw;
        try {
            raw = redisTemplate.opsForValue().get(buildKey(cacheName, key));
        } catch (DataAccessException e) {
            log.warn("cache read failed cacheName={} key={} - treating as miss", cacheName, key, e);
            return Optional.empty();
        }
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, type));
        } catch (JsonProcessingException e) {
            log.warn("cache deserialize failed cacheName={} key={} - treating as miss", cacheName, key, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> List<T> multiGet(String cacheName, java.util.List<String> keys, Class<T> type) {
        if (keys == null || keys.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        if (!cacheProperties.enabled()) {
            return java.util.Collections.nCopies(keys.size(), null);
        }
        List<String> fullKeys = keys.stream().map(k -> buildKey(cacheName, k)).toList();
        List<String> rawValues;
        try {
            rawValues = redisTemplate.opsForValue().multiGet(fullKeys);
        } catch (DataAccessException e) {
            log.warn("cache multiGet failed cacheName={} - treating as miss", cacheName, e);
            return java.util.Collections.nCopies(keys.size(), null);
        }
        if (rawValues == null) {
            return java.util.Collections.nCopies(keys.size(), null);
        }
        List<T> result = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            String raw = rawValues.get(i);
            if (raw == null) {
                result.add(null);
                continue;
            }
            try {
                result.add(objectMapper.readValue(raw, type));
            } catch (JsonProcessingException e) {
                log.warn("cache deserialize failed cacheName={} key={} - treating as miss", cacheName, keys.get(i), e);
                result.add(null);
            }
        }
        return result;
    }

    @Override
    public void put(String cacheName, String key, Object value, Duration ttl) {
        if (!cacheProperties.enabled()) {
            return;
        }
        String raw;
        try {
            raw = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("cache serialize failed cacheName={} key={} - skipping cache write", cacheName, key, e);
            return;
        }
        try {
            redisTemplate.opsForValue().set(buildKey(cacheName, key), raw, ttl);
        } catch (DataAccessException e) {
            log.warn("cache write failed cacheName={} key={} - skipping cache write", cacheName, key, e);
        }
    }

    @Override
    public void multiPut(String cacheName, Map<String, Object> values, Duration ttl) {
        if (values == null || values.isEmpty() || !cacheProperties.enabled()) {
            return;
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            put(cacheName, entry.getKey(), entry.getValue(), ttl);
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        if (!cacheProperties.enabled()) {
            return;
        }
        try {
            redisTemplate.delete(buildKey(cacheName, key));
        } catch (DataAccessException e) {
            log.warn("cache evict failed cacheName={} key={}", cacheName, key, e);
        }
    }

    @Override
    public void evictAll(String cacheName) {
        if (!cacheProperties.enabled()) {
            return;
        }
        deleteByPattern(namespace(cacheName) + "*");
    }

    @Override
    public void evictByPrefix(String cacheName, String keyPrefix) {
        if (!cacheProperties.enabled()) {
            return;
        }
        deleteByPattern(buildKey(cacheName, keyPrefix) + "*");
    }

    private void deleteByPattern(String pattern) {
        Set<String> keysToDelete = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(500).build();
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .keyCommands()
                .scan(scanOptions)) {
            while (cursor.hasNext()) {
                keysToDelete.add(new String(cursor.next()));
            }
        }
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }

    private String buildKey(String cacheName, String key) {
        return namespace(cacheName) + key;
    }

    private String namespace(String cacheName) {
        return cacheProperties.keyPrefix() + ":" + cacheName + "::";
    }
}
