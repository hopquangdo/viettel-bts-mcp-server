package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.AppLoggingProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.logging.request-trace.enabled", havingValue = "true")
public class RequestFlowTracingAspect {

    private static final ThreadLocal<Integer> CALL_DEPTH = ThreadLocal.withInitial(() -> 0);

    private final AppLoggingProperties loggingProperties;

    @Around(
            "execution(* vn.edu.huce.iic.bts_ops_platform.handler..*(..))"
                + " || execution(* vn.edu.huce.iic.bts_ops_platform.repository..*(..))")
    public Object traceRequestFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        int depth = CALL_DEPTH.get();
        String indent = "  ".repeat(depth);
        String signature = joinPoint.getSignature().toShortString();
        String argsSuffix = formatArgs(joinPoint.getArgs());
        long startNanos = System.nanoTime();

        log.info("{}{} → {}{}", indent, layerLabel(joinPoint), signature, argsSuffix);

        CALL_DEPTH.set(depth + 1);
        Object result = null;
        Throwable thrown = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            thrown = ex;
            throw ex;
        } finally {
            long endNanos = System.nanoTime();
            long selfNanos = endNanos - startNanos;
            CALL_DEPTH.set(depth);

            String selfDuration = RequestTraceContext.formatDuration(selfNanos);
            String totalDuration = RequestTraceContext.formatElapsedSinceRequest(endNanos);
            String timing = totalDuration == null
                    ? "self=" + selfDuration
                    : "self=" + selfDuration + ", total=" + totalDuration;

            if (thrown != null) {
                log.warn(
                        "{}{} ✗ {} | {} | error={}",
                        indent,
                        layerLabel(joinPoint),
                        signature,
                        timing,
                        thrown.getClass().getSimpleName());
            } else {
                try {
                    String resultSuffix = formatResult(result);
                    log.info(
                            "{}{} ← {} | {} | result={}",
                            indent,
                            layerLabel(joinPoint),
                            signature,
                            timing,
                            resultSuffix);
                } catch (RuntimeException ex) {
                    log.warn(
                            "{}{} ← {} | {} | result=<format-error: {}>",
                            indent,
                            layerLabel(joinPoint),
                            signature,
                            timing,
                            ex.toString());
                }
            }
        }
    }

    private String formatArgs(Object[] args) {
        if (!loggingProperties.requestTrace().logArgs() || args == null || args.length == 0) {
            return "";
        }
        String rendered = Arrays.stream(args)
                .map(arg -> arg == null ? "null" : String.valueOf(arg))
                .collect(Collectors.joining(", "));
        return " args=[" + rendered + "]";
    }

    private String formatResult(Object result) {
        if (!loggingProperties.requestTrace().logResult()) {
            return "-";
        }
        return TraceValueFormatter.format(result, loggingProperties.requestTrace().maxResultLength());
    }

    private static String layerLabel(ProceedingJoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();
        if (typeName.contains(".controller.")) {
            return "[controller] ";
        }
        if (typeName.contains(".service.impl.")) {
            return "[service]    ";
        }
        if (typeName.contains(".repository.")) {
            return "[repository] ";
        }
        return "[internal]   ";
    }
}
