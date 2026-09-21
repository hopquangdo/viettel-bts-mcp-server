package vn.edu.huce.iic.bts_ops_platform.common.util;

import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class EntityFilter {

    private EntityFilter() {
    }

    public static String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    /** Escape ký tự đại diện SQL LIKE (% _) trước khi ghép vào CONCAT('%', :keyword, '%'). */
    public static String escapeLikeWildcards(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /** Chuẩn hóa từ khóa tìm kiếm dùng với LIKE … ESCAPE '\\'. */
    public static String normalizeSearchForLike(String value) {
        return escapeLikeWildcards(normalizeSearch(value));
    }

    public static String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    public static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static boolean matchesKeyword(String keyword, String... fields) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String content = String.join(" ", fields).toLowerCase();
        return content.contains(keyword);
    }

    public static <T> boolean isActive(T entity, Function<T, Boolean> hoatDongGetter, Boolean activeOnly) {
        if (activeOnly == null || !activeOnly) {
            return true;
        }
        return Boolean.TRUE.equals(hoatDongGetter.apply(entity));
    }

    public static <T> boolean isNotDeleted(T entity, Function<T, Instant> ngayXoaGetter, boolean includeDeleted) {
        if (includeDeleted) {
            return true;
        }
        Instant ngayXoa = ngayXoaGetter.apply(entity);
        return ngayXoa == null;
    }

    public static <T> Predicate<T> combine(Predicate<T>... predicates) {
        Predicate<T> combined = t -> true;
        for (Predicate<T> predicate : predicates) {
            combined = combined.and(predicate);
        }
        return combined;
    }

    public static void softDelete(InstantSetter setter, Runnable deactivate) {
        setter.set(Instant.now());
        deactivate.run();
    }

    @FunctionalInterface
    public interface InstantSetter {
        void set(Instant value);
    }

    public static <T> T requireFound(java.util.Optional<T> optional, Supplier<? extends RuntimeException> exception) {
        return optional.orElseThrow(exception);
    }

    public static int normalizePage(Integer page) {
        return (page == null || page < 0) ? 0 : page;
    }

    public static int normalizeSize(Integer size, int defaultSize, int maxSize) {
        if (size == null || size <= 0) {
            return defaultSize;
        }
        return Math.min(size, maxSize);
    }
}
