package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

import java.util.List;

/** Kết quả danh sách của tool, gồm dữ liệu và toàn bộ metadata phân trang. */
public record PagedResult<T>(List<T> items, int page, int pageSize, long totalItems, int totalPages) {

    public static <T> PagedResult<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int totalPages = pageSize > 0 ? (int) Math.ceil(totalItems / (double) pageSize) : 0;
        return new PagedResult<>(List.copyOf(items), page, pageSize, totalItems, totalPages);
    }
}
