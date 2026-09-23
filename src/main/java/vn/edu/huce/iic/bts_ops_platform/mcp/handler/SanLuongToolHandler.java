package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;

import java.time.LocalDate;

/** Contract logic cho tool AI module Sản lượng — 1 method duy nhất, tự thu hẹp theo filter truyền vào. */
public interface SanLuongToolHandler {

    /** nhaThau: ID, mã hoặc tên nhà thầu; khuVuc/tinhThanh dùng FK chuẩn trên đối tượng hợp đồng. */
    SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                       String khuVuc, String tinhThanh, LocalDate fromDate, LocalDate toDate,
                                       Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                       String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan);
}
