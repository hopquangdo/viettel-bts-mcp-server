package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.response;

import java.util.List;

/** Paged status-list response; page is zero-based. */
public record BienBanTheoTrangThaiResponse(
        List<BienBanStatusItemResponse> danhSach,
        long totalItems,
        int page,
        int pageSize) {
    public BienBanTheoTrangThaiResponse {
        danhSach = List.copyOf(danhSach);
    }
}