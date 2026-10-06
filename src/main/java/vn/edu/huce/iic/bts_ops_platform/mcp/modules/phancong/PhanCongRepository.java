package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachResult;

/** Typed query boundary; deliberately has no Spring Data implementation or bean yet. */
public interface PhanCongRepository {

    PhanCongDanhSachResult danhSach(PhanCongDanhSachQuery query);
}