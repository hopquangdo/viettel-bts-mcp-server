package vn.edu.huce.iic.bts_ops_platform.support;

import org.springframework.data.domain.PageRequest;

/**
 * Chuẩn hóa page/pageSize (null-safe, clamp max) và tạo PageRequest cho repository.
 */
public final class PagingUtil {

    public static final int DEFAULT_PAGE_SIZE = 3;
    public static final int MAX_PAGE_SIZE = 100;

    private PagingUtil() {
    }

    public static int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 0;
    }

    public static int resolvePageSize(Integer pageSize, int defaultPageSize, int maxPageSize) {
        if (pageSize == null) {
            return defaultPageSize;
        }
        return Math.min(Math.max(pageSize, 1), maxPageSize);
    }

    /** PageRequest cho query repository — phân trang luôn được thực hiện ở DB. */
    public static PageRequest toPageRequest(Integer page, Integer pageSize, int defaultPageSize, int maxPageSize) {
        return PageRequest.of(resolvePage(page), resolvePageSize(pageSize, defaultPageSize, maxPageSize));
    }
}
