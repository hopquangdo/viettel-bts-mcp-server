package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonQueryResponse;

import java.time.LocalDate;
/** Contract logic cho tool AI module Trạm tồn (đối tượng tồn) — 1 method duy nhất. */
public interface TramTonToolHandler {

    TramTonQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                               String khuVuc, String tinhThanh, String loaiHopDong, String tab, LocalDate sinceDate, Integer quaHanNgay, Integer top,
                               Integer page, Integer pageSize, Integer soNgayThieuCapNhat,
                               LocalDate fromDate, LocalDate toDate);
}
