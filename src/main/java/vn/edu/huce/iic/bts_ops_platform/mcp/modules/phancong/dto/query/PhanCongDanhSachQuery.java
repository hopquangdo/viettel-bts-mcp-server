package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request.PhanCongFilter;

/** Normalized repository query; page is zero-based and keywordPattern uses SQL LIKE escaping. */
public record PhanCongDanhSachQuery(
        PhanCongFilter filter,
        String keywordPattern,
        int page,
        int pageSize) {
}