package vn.edu.huce.iic.bts_ops_platform.mcp.common.trang;

import java.util.List;

/** Kết quả danh sách gồm dữ liệu và metadata phân trang. */
public record Trang<T>(List<T> items, int page, int pageSize, long totalItems, int totalPages) {

    public static <T> Trang<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int totalPages = pageSize > 0 ? (int) Math.ceil(totalItems / (double) pageSize) : 0;
        return new Trang<>(List.copyOf(items), page, pageSize, totalItems, totalPages);
    }
}