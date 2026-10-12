package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.query.HopDongTienDoQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.request.HopDongFilter;

import java.util.List;

/** Persistence contract for the new module; intentionally has no Spring Data implementation yet. */
public interface HopDongRepository {

    List<HopDongTienDoQuery> findTienDoHopDong(HopDongFilter filter);
}