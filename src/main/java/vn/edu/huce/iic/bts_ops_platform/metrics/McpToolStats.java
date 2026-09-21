package vn.edu.huce.iic.bts_ops_platform.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thống kê hiệu năng từng tool MCP: số lần gọi, lỗi, thời gian (min/TB/p50/p95/max) và số câu SQL mỗi lần gọi.
 * Lưu trong bộ nhớ (cửa sổ {@value #WINDOW} lần gọi gần nhất cho phân vị) và đẩy sang Micrometer
 * ({@code mcp.tool.duration}, {@code mcp.tool.sql}). Chỉ chứa số liệu tổng hợp, không có tham số hay dữ liệu người dùng.
 * Mọi lỗi trong lớp này bị nuốt để không ảnh hưởng lời gọi tool.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mcp.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class McpToolStats {

    static final int WINDOW = 1000;

    private final Map<String, Series> series = new ConcurrentHashMap<>();
    private volatile Instant startedAt = Instant.now();
    private final long slowMs;
    private final int slowSql;
    private final MeterRegistry registry;

    public McpToolStats(@Autowired(required = false) MeterRegistry registry,
                        @Value("${mcp.metrics.slow-ms:1000}") long slowMs,
                        @Value("${mcp.metrics.slow-sql:20}") int slowSql) {
        this.registry = registry;
        this.slowMs = slowMs;
        this.slowSql = slowSql;
    }

    /** Ghi nhận 1 lần gọi tool ngoài cùng. */
    public void record(String tool, long nanos, int sql, boolean ok) {
        try {
            double ms = nanos / 1_000_000.0;
            series.computeIfAbsent(tool, Series::new).add(ms, sql, ok);
            if (registry != null) {
                Timer.builder("mcp.tool.duration").tag("tool", tool).tag("outcome", ok ? "ok" : "error")
                        .register(registry).record(Duration.ofNanos(nanos));
                DistributionSummary.builder("mcp.tool.sql").tag("tool", tool).register(registry).record(sql);
            }
            if (ms > slowMs || sql > slowSql) {
                log.warn("MCP_SLOW tool={} ms={} sql={} ok={}", tool, Math.round(ms), sql, ok);
            }
        } catch (RuntimeException e) {
            log.debug("Bỏ qua lỗi ghi thống kê MCP: {}", e.getMessage());
        }
    }

    /** Báo cáo hiện tại; {@code reset} xoá số liệu sau khi lấy để bắt đầu kỳ báo cáo mới. */
    public Report report(boolean reset) {
        List<Row> rows = new ArrayList<>();
        series.values().forEach(s -> rows.add(s.row()));
        rows.sort(Comparator.comparing(Row::tool));
        Report report = new Report(startedAt, Instant.now(), rows);
        if (reset) {
            series.clear();
            startedAt = Instant.now();
        }
        return report;
    }

    /** Mỗi 15 phút ghi 1 dòng tổng hợp cho từng tool vào log, làm báo cáo theo thời gian. */
    @Scheduled(fixedRateString = "${mcp.metrics.log-interval-ms:900000}", initialDelayString = "${mcp.metrics.log-interval-ms:900000}")
    void logSummary() {
        try {
            for (Row r : report(false).rows()) {
                log.info("MCP_STATS tool={} calls={} errors={} ms[min/avg/p50/p95/max]={}/{}/{}/{}/{} sql[avg/max]={}/{}",
                        r.tool(), r.calls(), r.errors(), r.minMs(), r.avgMs(), r.p50Ms(), r.p95Ms(), r.maxMs(), r.avgSql(), r.maxSql());
            }
        } catch (RuntimeException e) {
            log.debug("Bỏ qua lỗi ghi log thống kê MCP: {}", e.getMessage());
        }
    }

    public record Report(Instant since, Instant now, List<Row> rows) {
    }

    public record Row(String tool, long calls, long errors, double minMs, double avgMs, double p50Ms, double p95Ms,
                      double maxMs, double avgSql, int maxSql) {
    }

    private static final class Series {
        private final String tool;
        private final double[] window = new double[WINDOW];
        private int filled;
        private int next;
        private long calls;
        private long errors;
        private double sumMs;
        private double minMs = Double.MAX_VALUE;
        private double maxMs;
        private long sumSql;
        private int maxSql;

        private Series(String tool) {
            this.tool = tool;
        }

        synchronized void add(double ms, int sql, boolean ok) {
            calls++;
            if (!ok) {
                errors++;
            }
            sumMs += ms;
            minMs = Math.min(minMs, ms);
            maxMs = Math.max(maxMs, ms);
            sumSql += sql;
            maxSql = Math.max(maxSql, sql);
            window[next] = ms;
            next = (next + 1) % WINDOW;
            filled = Math.min(filled + 1, WINDOW);
        }

        synchronized Row row() {
            double[] copy = Arrays.copyOf(window, filled);
            Arrays.sort(copy);
            return new Row(tool, calls, errors, round(calls == 0 ? 0 : minMs), round(calls == 0 ? 0 : sumMs / calls),
                    round(percentile(copy, 0.50)), round(percentile(copy, 0.95)), round(maxMs),
                    round(calls == 0 ? 0 : (double) sumSql / calls), maxSql);
        }

        private static double percentile(double[] sorted, double p) {
            if (sorted.length == 0) {
                return 0;
            }
            int idx = (int) Math.ceil(p * sorted.length) - 1;
            return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
        }

        private static double round(double v) {
            return Math.round(v * 10.0) / 10.0;
        }
    }
}
