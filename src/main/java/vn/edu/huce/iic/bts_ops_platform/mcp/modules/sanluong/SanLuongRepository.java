package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query.SanLuongProgressGroupQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.request.SanLuongFilter;

import java.util.List;

/** Typed persistence contract; intentionally has no Spring Data implementation or bean yet. */
public interface SanLuongRepository {

    List<SanLuongProgressGroupQuery> findProgressGroups(SanLuongFilter filter);
}