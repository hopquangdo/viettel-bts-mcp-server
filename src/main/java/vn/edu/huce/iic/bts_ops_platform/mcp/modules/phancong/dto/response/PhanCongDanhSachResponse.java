package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response;

import java.util.List;

/** Paginated assignment-list response. */
public record PhanCongDanhSachResponse(
        List<PhanCongItemResponse> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    public PhanCongDanhSachResponse {
        items = List.copyOf(items);
    }
}