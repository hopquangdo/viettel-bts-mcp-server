package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongDanhSachTinhChiTietResponse {
    private String khuVuc;
    private String tinh;
    private String nhaThau;
    private UUID doiTuongQuanLyId;
    private String doiTuongQuanLyMa;
    private String doiTuongQuanLyTen;
    private List<LuongTrangThaiBuocResponse> luongTrangThaiBuoc = new ArrayList<>();
    private List<HopDongDanhSachThuocTinhColumnResponse> thuocTinhColumns = new ArrayList<>();
    private HopDongDanhSachTinhChiTietSummaryResponse summary = new HopDongDanhSachTinhChiTietSummaryResponse();
    private List<HopDongDanhSachTienDoBuocResponse> tienDoTheoTrangThai = new ArrayList<>();
    private List<HopDongDanhSachTramRowResponse> tramRows = new ArrayList<>();
}
