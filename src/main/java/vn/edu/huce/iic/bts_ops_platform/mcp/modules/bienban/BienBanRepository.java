package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.query.BienBanStatusPageQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.request.BienBanFilter;

/** Typed persistence port; no Spring Data implementation or bean is registered yet. */
public interface BienBanRepository {

    BienBanStatusPageQuery findTheoTrangThai(BienBanFilter filter, int page, int pageSize);
}