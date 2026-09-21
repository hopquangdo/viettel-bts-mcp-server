package vn.edu.huce.iic.bts_ops_platform.infrastructure.logging;

final class RequestTraceContext {

    private static final ThreadLocal<Long> REQUEST_START_NANOS = new ThreadLocal<>();

    private RequestTraceContext() {}

    static void markRequestStart() {
        REQUEST_START_NANOS.set(System.nanoTime());
    }

    static void clear() {
        REQUEST_START_NANOS.remove();
    }

    static String formatElapsedSinceRequest(long endNanos) {
        Long startNanos = REQUEST_START_NANOS.get();
        if (startNanos == null) {
            return null;
        }
        return formatDuration(endNanos - startNanos);
    }

    static String formatDuration(long nanos) {
        double millis = nanos / 1_000_000.0;
        if (millis < 1.0) {
            return String.format("%.2fms", millis);
        }
        if (millis < 100.0) {
            return String.format("%.1fms", millis);
        }
        return millis < 1_000.0
                ? String.format("%.0fms", millis)
                : String.format("%.2fs", millis / 1_000.0);
    }
}
