package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query;

import java.util.List;

/** Rows and total count returned by the repository for one page. */
public record PhanCongDanhSachResult(List<PhanCongRow> rows, long totalItems) {

    public PhanCongDanhSachResult {
        rows = List.copyOf(rows);
    }
}