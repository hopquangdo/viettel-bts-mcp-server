package vn.edu.huce.iic.bts_ops_platform.metrics;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Đếm số câu SQL Hibernate chuẩn bị, chỉ trong lúc một lần gọi tool đang chạy ({@link #begin()} / {@link #end()}).
 * Ngoài khoảng đó không làm gì, và không bao giờ sửa câu SQL. Bộ đếm dùng chung được cho các luồng phụ của cùng lần gọi
 * ({@link #capture()} / {@link #attach}) để vẫn đếm đủ khi truy vấn chạy song song.
 */
public class McpSqlCounter implements StatementInspector {

    private static final ThreadLocal<AtomicInteger> CURRENT = new ThreadLocal<>();

    /** Bắt đầu đếm cho luồng hiện tại; trả về false nếu đã có lượt đếm ngoài cùng (lời gọi lồng nhau). */
    public static boolean begin() {
        if (CURRENT.get() != null) {
            return false;
        }
        CURRENT.set(new AtomicInteger());
        return true;
    }

    /** Kết thúc lượt đếm ngoài cùng và trả về số câu SQL đã đếm. */
    public static int end() {
        AtomicInteger counter = CURRENT.get();
        CURRENT.remove();
        return counter == null ? 0 : counter.get();
    }

    /** Bộ đếm của lần gọi hiện tại (null nếu không có), để luồng phụ dùng chung. */
    public static AtomicInteger capture() {
        return CURRENT.get();
    }

    /** Gắn bộ đếm của lần gọi cha vào luồng phụ. */
    public static void attach(AtomicInteger counter) {
        if (counter != null) {
            CURRENT.set(counter);
        }
    }

    /** Gỡ bộ đếm khỏi luồng phụ sau khi xong việc. */
    public static void detach() {
        CURRENT.remove();
    }

    @Override
    public String inspect(String sql) {
        AtomicInteger counter = CURRENT.get();
        if (counter != null) {
            counter.incrementAndGet();
        }
        return sql;
    }
}
