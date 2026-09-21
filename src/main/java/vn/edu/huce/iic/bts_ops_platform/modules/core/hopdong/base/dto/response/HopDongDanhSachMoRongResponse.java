package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;

import java.util.ArrayList;
import java.util.List;

@Data
public class HopDongDanhSachMoRongResponse {
    private List<LuongTrangThaiBuocResponse> luongTrangThaiBuoc = new ArrayList<>();
    private List<HopDongDanhSachNhomUuTienItemResponse> nhomUuTiens = new ArrayList<>();
    private List<HopDongDanhSachKhuVucResponse> khuVucs = new ArrayList<>();
}
