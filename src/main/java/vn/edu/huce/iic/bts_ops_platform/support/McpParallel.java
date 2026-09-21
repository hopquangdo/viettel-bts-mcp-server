package vn.edu.huce.iic.bts_ops_platform.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.metrics.McpSqlCounter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Chạy song song các truy vấn ĐỘC LẬP của một lần gọi tool để độ trễ chỉ còn bằng nhánh chậm nhất thay vì tổng các nhánh
 * (mỗi câu SQL tốn ít nhất 1 lần khứ hồi tới DB).
 * <p>
 * Quy tắc dùng: chỉ đưa vào các câu TRUY VẤN LÁ (repository) từ luồng gọi tool, không lồng {@code async} trong tác vụ
 * đang chạy ở luồng phụ (tránh chờ nhau làm cạn luồng). Tác vụ phụ KHÔNG có {@code SecurityContext}: mọi thứ phụ thuộc người
 * dùng (phạm vi khu vực, phân quyền) phải được tính ở luồng gọi tool trước rồi truyền vào tham số truy vấn. Hàng đợi đầy thì
 * tác vụ chạy ngay ở luồng gọi (CallerRuns), nên không bao giờ mất việc hay treo.
 * <p>
 * Handler không nên giữ giao dịch DB mở trong lúc chờ (mỗi truy vấn tự lấy kết nối ngắn hạn), nếu không luồng gọi giữ 1 kết nối
 * và chờ các luồng phụ cần thêm kết nối, gây nghẽn khi tải cao.
 */
@Component
public class McpParallel implements DisposableBean {

    private final ExecutorService executor;

    public McpParallel(@Value("${mcp.parallel.max-threads:16}") int maxThreads,
                       @Value("${mcp.parallel.queue:64}") int queue) {
        AtomicInteger seq = new AtomicInteger();
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "mcp-q-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        ThreadPoolExecutor pool = new ThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queue), factory, new ThreadPoolExecutor.CallerRunsPolicy());
        pool.allowCoreThreadTimeOut(true);
        this.executor = pool;
    }

    /** Chạy {@code task} ở luồng phụ, giữ bộ đếm SQL của lần gọi hiện tại. */
    public <T> CompletableFuture<T> async(Supplier<T> task) {
        AtomicInteger counter = McpSqlCounter.capture();
        return CompletableFuture.supplyAsync(() -> {
            McpSqlCounter.attach(counter);
            try {
                return task.get();
            } finally {
                McpSqlCounter.detach();
            }
        }, executor);
    }

    /** Chờ kết quả; ném lại đúng ngoại lệ gốc (vd AppException) thay vì CompletionException. */
    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            if (cause instanceof Error err) {
                throw err;
            }
            throw e;
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }
}
