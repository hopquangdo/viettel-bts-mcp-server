package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.VuongMacQueryResponse;

import java.time.LocalDate;
/** Contract logic cho tool AI module Vướng mắc — 1 method duy nhất. */
public interface VuongMacToolHandler {

    VuongMacQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh, String loaiHopDong,
                                String trangThai, String kieuVuongMac, String giaiDoan, Boolean dangMoOnly,
                                String query, Integer top, LocalDate sinceDate,
                                Integer page, Integer pageSize, Integer quaHanNgay);
}
