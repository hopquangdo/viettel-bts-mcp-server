package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int pageSize;
    private long total;
    private int totalPages;

    public static <T> PageResponse<T> ofItems(List<T> items, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil(totalElements / (double) pageSize);
        return PageResponse.<T>builder()
                .items(items)
                .pageSize(pageSize)
                .page(pageNumber)
                .total(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
