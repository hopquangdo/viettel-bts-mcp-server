package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongQueryResponse;

/** Contract logic cho tool AI module Phân công — 1 method duy nhất. */
public interface PhanCongToolHandler {

    PhanCongQueryResponse query(String vung, String nhaThau, String canBo,
                                String query, String hopDong, String khuVuc, String tinhThanh, String doiTuong,
                                String giaiDoan, Boolean lichSu, Integer top,
                                Integer page, Integer pageSize);
}
