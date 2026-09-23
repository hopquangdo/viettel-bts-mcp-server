package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauQueryResponse;

/** Contract logic cho tool AI Nhà thầu — liệt kê các nhà thầu, 1 method duy nhất. */
public interface NhaThauToolHandler {

    NhaThauQueryResponse query(String query, String hopDong, String khuVuc, String tinhThanh,
                               Integer page, Integer pageSize);
}
