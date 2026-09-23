package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongQueryResponse;

/** Contract logic cho tool AI module Hợp đồng — 1 method duy nhất, tự thu hẹp theo filter truyền vào. */
public interface HopDongToolHandler {

    HopDongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                               String khuVuc, String tinhThanh, String loaiHopDong, String kieuHopDong, String query, Integer page, Integer pageSize,
                               Double nguongChamTienDo, Double nguongXanh, Double nguongVang,
                               Integer top, java.time.LocalDate fromDate, java.time.LocalDate toDate, String loaiNgay);
}
