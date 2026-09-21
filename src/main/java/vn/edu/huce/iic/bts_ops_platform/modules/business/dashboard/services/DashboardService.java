package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.services;

import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardChiTietTrungTamResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardSanLuongBatThuongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLinhVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLoaiItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuTheSanLuongResponse;

import java.util.List;

public interface DashboardService {

    DashboardTongQuanResponse tongQuan(Integer nam, Integer thang);

    List<DashboardTheoLoaiItemResponse> theoLoai();

    List<DashboardSanLuongBatThuongItemResponse> sanLuongBatThuong(Integer limit);

    DashboardXuTheSanLuongResponse xuTheSanLuong(
            String timeMode,
            String granularity,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId,
            String loaiHopDongId);

    DashboardChiTietTrungTamResponse chiTietTrungTam(
            String timeMode,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId,
            String loaiHopDongId);

    /**
     * Q4 theo lĩnh vực: chỉ lọc theo hợp đồng (hopDongId).
     * Lọc lĩnh vực (loaiHopDongId) xử lý highlight phía FE, không slice dữ liệu API.
     */
    DashboardTheoLinhVucResponse theoLinhVuc(
            String timeMode,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId);
}
