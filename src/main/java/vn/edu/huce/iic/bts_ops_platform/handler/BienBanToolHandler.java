package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.bienban.BienBanQueryResponse;

/** Contract logic cho tool AI module Biên bản — 1 method duy nhất. */
public interface BienBanToolHandler {

    BienBanQueryResponse query(String khuVuc, String tinhThanh, String hopDong, String nhaThau, String trangThai,
                               Integer page, Integer pageSize);
}
