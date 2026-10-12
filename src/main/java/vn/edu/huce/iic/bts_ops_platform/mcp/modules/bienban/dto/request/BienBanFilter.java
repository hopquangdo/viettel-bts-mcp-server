package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.request;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.KyLoc;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.PhamViLoc;

/** Raw scope, status, and date inputs for Biên bản listing. */
public record BienBanFilter(PhamViLoc phamVi, String trangThai, KyLoc ky) {
}