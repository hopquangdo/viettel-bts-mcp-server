package vn.edu.huce.iic.bts_ops_platform.handler;

import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.HoSoDoiTuongQueryResponse;

/** Contract logic cho tool AI hosodoituong_tool — 1 method duy nhất, tự thu hẹp theo filter truyền vào. */
public interface HoSoDoiTuongToolHandler {

    HoSoDoiTuongQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh,
                                    String trangThaiHopDong, Boolean coNhomUuTien, Integer page, Integer pageSize);
}
