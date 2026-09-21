package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoQueryResponse;

/** Contract logic cho tool AI module Ngân sách hợp đồng (Volume) — 1 method duy nhất. */
public interface NganHoToolHandler {

    NganHoQueryResponse query(String hopDong, String query, Double heSo, Double nguongCanhBao,
                              String loaiHopDong, String statusFilter, Integer page, Integer pageSize);
}
