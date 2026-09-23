package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongQueryResponse;

/** Contract logic khối hoSoDoiTuong của tool AI doituong_tool (trước là hosodoituong_tool) — 1 method duy nhất, tự thu hẹp theo filter truyền vào. */
public interface HoSoDoiTuongToolHandler {

    HoSoDoiTuongQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh,
                                    String trangThaiHopDong, Boolean coNhomUuTien,
            java.time.LocalDate fromDate, java.time.LocalDate toDate, Integer page, Integer pageSize);
}
