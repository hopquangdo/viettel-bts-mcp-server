package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chống dò mật khẩu (ATTT PL02#26): đếm số lần đăng nhập sai theo (tên đăng nhập + IP), quá
 * ngưỡng thì khóa tạm thời. Lưu trong bộ nhớ tiến trình — đủ cho giai đoạn 1 node; restart sẽ
 * reset (chấp nhận được, sẽ nâng lên Redis khi chạy nhiều instance).
 */
@Slf4j
@Service
public class LoginAttemptService {

    @Value("${app.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login.lock-minutes:10}")
    private long lockMinutes;

    private static final class Attempt {
        int count;
        Instant lockedUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** true nếu (tài khoản+IP) đang bị khóa — chặn login trước cả khi kiểm mật khẩu. */
    public boolean isBlocked(String username, String ip) {
        Attempt attempt = attempts.get(key(username, ip));
        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.lockedUntil)) {
            attempts.remove(key(username, ip));
            return false;
        }
        return true;
    }

    /** Số phút còn lại của lần khóa hiện tại (để báo cho người dùng). */
    public long remainingLockMinutes(String username, String ip) {
        Attempt attempt = attempts.get(key(username, ip));
        if (attempt == null || attempt.lockedUntil == null) {
            return 0;
        }
        long minutes = Duration.between(Instant.now(), attempt.lockedUntil).toMinutes();
        return Math.max(1, minutes);
    }

    /** Ghi nhận 1 lần sai; trả về true nếu vừa chạm ngưỡng và bị khóa. */
    public boolean recordFailure(String username, String ip) {
        Attempt attempt = attempts.computeIfAbsent(key(username, ip), k -> new Attempt());
        attempt.count += 1;
        if (attempt.count >= maxAttempts) {
            attempt.lockedUntil = Instant.now().plus(Duration.ofMinutes(lockMinutes));
            attempt.count = 0;
            log.warn("Khóa đăng nhập {} phút do sai quá {} lần: user={} ip={}",
                    lockMinutes, maxAttempts, username, ip);
            return true;
        }
        return false;
    }

    /** Đăng nhập thành công — xóa bộ đếm. */
    public void recordSuccess(String username, String ip) {
        attempts.remove(key(username, ip));
    }

    private static String key(String username, String ip) {
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + (ip == null ? "" : ip);
    }
}
