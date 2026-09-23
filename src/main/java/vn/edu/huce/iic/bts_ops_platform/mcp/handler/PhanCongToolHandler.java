package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong.PhanCongQueryResponse;

/** Contract logic cho tool AI module Phân công — 1 method duy nhất. */
public interface PhanCongToolHandler {

    PhanCongQueryResponse query(String vung, String nhaThau, String canBo,
                                String query, String hopDong, String khuVuc, String tinhThanh, String doiTuong,
                                String giaiDoan, Boolean lichSu, Integer top,
                                Integer page, Integer pageSize,
            java.time.LocalDate fromDate, java.time.LocalDate toDate);
}
