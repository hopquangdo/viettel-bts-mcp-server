package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.request;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.PhamViLoc;

/** Raw filter inputs for contract-module queries. */
public record HopDongFilter(PhamViLoc phamVi, String kieuHopDong) {
}