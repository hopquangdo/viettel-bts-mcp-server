package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau;

import java.util.List;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

public interface NhaThauService {
    NhaThauQueryResponse query(String query, String hopDong, String khuVuc, String tinhThanh,
                                     Integer page, Integer pageSize);
}
