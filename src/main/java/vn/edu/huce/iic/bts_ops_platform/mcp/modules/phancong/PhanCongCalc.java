package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request.PhanCongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response.PhanCongDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response.PhanCongItemResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pure assignment-list filter and pagination calculations. */
public final class PhanCongCalc {

    public static final int DEFAULT_PAGE_SIZE = 3;
    public static final int MAX_PAGE_SIZE = 100;

    private PhanCongCalc() {
    }

    public static PhanCongDanhSachQuery toQuery(PhanCongFilter filter, Integer page, Integer pageSize) {
        PhanCongFilter resolvedFilter = filter != null ? filter : PhanCongFilter.empty();
        return new PhanCongDanhSachQuery(
                resolvedFilter,
                likePattern(resolvedFilter.query()),
                resolvePage(page),
                resolvePageSize(pageSize));
    }

    public static PhanCongDanhSachResponse toResponse(
            PhanCongDanhSachResult result,
            int page,
            int pageSize) {
        List<PhanCongItemResponse> items = new ArrayList<>(result.rows().size());
        for (PhanCongRow row : result.rows()) {
            items.add(new PhanCongItemResponse(row.id(), row.hopDongId(), row.hopDongDoiTuongId(),
                    row.nhaThau(), row.giaiDoan(), row.khuVucId(), row.maVung(), row.tinhThanhId(),
                    row.hoatDong(), row.ngayTao()));
        }

        long totalItems = Math.max(0, result.totalItems());
        int totalPages = (int) Math.min(Integer.MAX_VALUE,
                Math.ceil(totalItems / (double) pageSize));
        return new PhanCongDanhSachResponse(List.copyOf(items), page, pageSize, totalItems, totalPages,
                totalPages > 0 && page < totalPages - 1, page > 0);
    }

    public static int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 0;
    }

    public static int resolvePageSize(Integer pageSize) {
        return pageSize == null
                ? DEFAULT_PAGE_SIZE
                : Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
    }

    static String likePattern(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + normalized + "%";
    }
}