package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.query.VuongMacTongQuanQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request.VuongMacFilter;

/** Typed repository contract; intentionally has no Spring Data implementation or bean yet. */
public interface VuongMacRepository {

    VuongMacTongQuanQuery tongQuan(VuongMacFilter filter);
}