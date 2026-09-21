package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.nguonviec.NguonViecQueryResponse;

import java.time.LocalDate;
/** Contract logic cho tool AI module Nguồn việc — 1 method duy nhất. */
public interface NguonViecToolHandler {

    NguonViecQueryResponse query(String khuVuc, String nhaThau, String trangThai, String loaiCv, String phapLy,
                                 String linhVuc, String tab,
                                 LocalDate fromDate, LocalDate toDate, String query, Integer top,
                                 Integer page, Integer pageSize, Double nguongSapHet);
}
