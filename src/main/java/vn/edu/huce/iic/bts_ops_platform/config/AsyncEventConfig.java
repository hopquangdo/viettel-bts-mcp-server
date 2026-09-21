package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Executor riêng cho các @TransactionalEventListener "tự vá dữ liệu nền" (ví dụ
 * SnapshotDataMissingEvent) — các listener này không được phép chặn thread HTTP của request
 * gốc đã kích hoạt event (đặc biệt khi DB ở xa qua mạng, mỗi query tốn round-trip đáng kể).
 */
@Configuration
@EnableAsync
public class AsyncEventConfig {

    public static final String SNAPSHOT_REPAIR_EXECUTOR = "snapshotRepairExecutor";

    @Bean(SNAPSHOT_REPAIR_EXECUTOR)
    public Executor snapshotRepairExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        // Cache lạnh (restart backend, hoặc lần đầu quét toàn bộ hệ thống — vd
        // sanluong_theo_khuvuc_nhathau, tramton_tongquan) có thể phát sinh SnapshotDataMissingEvent
        // cho HÀNG NGHÌN đối tượng cùng lúc (~6888+ đối tượng đang hoạt động ở quy mô hiện tại).
        // Queue cũ (200) bị tràn gần như ngay lập tức → RejectedExecutionException hàng loạt,
        // làm hỏng lây cả các request KHÔNG liên quan cùng dùng chung executor này trong lúc đó.
        // Đây là việc vá dữ liệu nền best-effort (không chặn response chính), nên chấp nhận queue
        // lớn thay vì reject.
        executor.setQueueCapacity(20000);
        executor.setThreadNamePrefix("snapshot-repair-");
        executor.initialize();
        return executor;
    }
}
