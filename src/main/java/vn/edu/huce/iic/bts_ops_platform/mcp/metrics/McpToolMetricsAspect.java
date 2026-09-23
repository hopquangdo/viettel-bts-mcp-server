package vn.edu.huce.iic.bts_ops_platform.mcp.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Đo thời gian và số câu SQL của mỗi lần gọi tool (phương thức {@code query} của các *ToolHandlerImpl), cả khi gọi qua
 * MCP lẫn qua endpoint kiểm thử. Chỉ đo lời gọi ngoài cùng, lời gọi lồng nhau (nhathau, system_overview gọi tool khác)
 * được tính vào lời gọi ngoài. Không đổi tham số, kết quả hay ngoại lệ; lỗi khi ghi số liệu bị nuốt.
 */
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mcp.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class McpToolMetricsAspect {

    private final McpToolStats stats;

    @Around("execution(public * vn.edu.huce.iic.bts_ops_platform.handler.impls.*ToolHandlerImpl.query(..))")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean outermost = McpSqlCounter.begin();
        if (!outermost) {
            return joinPoint.proceed();
        }
        long start = System.nanoTime();
        boolean ok = false;
        try {
            Object result = joinPoint.proceed();
            ok = true;
            return result;
        } finally {
            long nanos = System.nanoTime() - start;
            int sql = McpSqlCounter.end();
            stats.record(toolName(joinPoint), nanos, sql, ok);
        }
    }

    private static String toolName(ProceedingJoinPoint joinPoint) {
        String name = joinPoint.getTarget().getClass().getSimpleName();
        return name.replace("ToolHandlerImpl", "").toLowerCase();
    }
}
