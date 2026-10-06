package vn.edu.huce.iic.bts_ops_platform.mcp.common.trang;

import org.springframework.data.domain.PageRequest;

/** Chuẩn hóa tham số phân trang cho các truy vấn MCP. */
public final class PhanTrang {

    public static final int DEFAULT_PAGE_SIZE = 3;
    public static final int MAX_PAGE_SIZE = 100;

    private PhanTrang() {
    }

    public static int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 0;
    }

    public static int resolvePageSize(Integer pageSize) {
        return resolvePageSize(pageSize, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public static int resolvePageSize(Integer pageSize, int defaultPageSize, int maxPageSize) {
        if (pageSize == null) {
            return defaultPageSize;
        }
        return Math.min(Math.max(pageSize, 1), maxPageSize);
    }

    public static PageRequest toPageRequest(Integer page, Integer pageSize) {
        return PageRequest.of(resolvePage(page), resolvePageSize(pageSize));
    }

    public static PageRequest toPageRequest(Integer page, Integer pageSize, int defaultPageSize, int maxPageSize) {
        return PageRequest.of(resolvePage(page), resolvePageSize(pageSize, defaultPageSize, maxPageSize));
    }
}