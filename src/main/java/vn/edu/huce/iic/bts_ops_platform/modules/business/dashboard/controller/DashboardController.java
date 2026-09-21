package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardChiTietTrungTamResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardSanLuongBatThuongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLinhVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLoaiItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuTheSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.services.DashboardService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.Dashboard.TAG, description = ApiEntityCatalog.Dashboard.DESCRIPTION)
@RequiresPermission(anyOf = {
        QuyenHanMa.QUAN_LY_HOP_DONG,
        QuyenHanMa.QUAN_LY_SAN_LUONG
})
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/tong-quan")
    @Operation(summary = "Chỉ số tổng quan dashboard (đối tượng, HĐ, vướng mắc, khu vực, sản lượng năm/tháng)")
    public ResponseEntity<ApiResponse<DashboardTongQuanResponse>> tongQuan(
            @RequestParam(required = false) Integer nam,
            @RequestParam(required = false) Integer thang) {
        return ApiResponse.<DashboardTongQuanResponse>build()
                .withData(dashboardService.tongQuan(nam, thang))
                .toEntity();
    }

    @GetMapping("/theo-loai")
    @Operation(summary = "Thống kê theo từng loại hợp đồng (thẻ summary)")
    public ResponseEntity<ApiResponse<List<DashboardTheoLoaiItemResponse>>> theoLoai() {
        return ApiResponse.<List<DashboardTheoLoaiItemResponse>>build()
                .withData(dashboardService.theoLoai())
                .toEntity();
    }

    @GetMapping("/san-luong-bat-thuong")
    @Operation(summary = "Danh sách trạm có sản lượng bất thường (vượt ngưỡng bình quân theo hợp đồng)")
    public ResponseEntity<ApiResponse<List<DashboardSanLuongBatThuongItemResponse>>> sanLuongBatThuong(
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.<List<DashboardSanLuongBatThuongItemResponse>>build()
                .withData(dashboardService.sanLuongBatThuong(limit))
                .toEntity();
    }

    @GetMapping("/xu-the-san-luong")
    @Operation(summary = "Xu thế sản lượng theo khu vực (filter UI: timeMode + granularity + HĐ/lĩnh vực)")
    public ResponseEntity<ApiResponse<DashboardXuTheSanLuongResponse>> xuTheSanLuong(
            @RequestParam(required = false) String timeMode,
            @RequestParam(required = false, defaultValue = "week") String granularity,
            @RequestParam(required = false) Integer nam,
            @RequestParam(required = false) Integer thang,
            @RequestParam(required = false) Integer tuan,
            @RequestParam(required = false) Integer quy,
            @RequestParam(required = false) String ngay,
            @RequestParam(required = false) String hopDongId,
            @RequestParam(required = false) String loaiHopDongId) {
        return ApiResponse.<DashboardXuTheSanLuongResponse>build()
                .withData(dashboardService.xuTheSanLuong(
                        timeMode, granularity, nam, thang, tuan, quy, ngay, hopDongId, loaiHopDongId))
                .toEntity();
    }

    @GetMapping("/chi-tiet-trung-tam")
    @Operation(summary = "Chi tiết sản lượng từng trung tâm theo tỉnh (filter UI timeMode + HĐ/lĩnh vực)")
    public ResponseEntity<ApiResponse<DashboardChiTietTrungTamResponse>> chiTietTrungTam(
            @RequestParam(required = false, defaultValue = "month") String timeMode,
            @RequestParam(required = false) Integer nam,
            @RequestParam(required = false) Integer thang,
            @RequestParam(required = false) Integer tuan,
            @RequestParam(required = false) Integer quy,
            @RequestParam(required = false) String ngay,
            @RequestParam(required = false) String hopDongId,
            @RequestParam(required = false) String loaiHopDongId) {
        return ApiResponse.<DashboardChiTietTrungTamResponse>build()
                .withData(dashboardService.chiTietTrungTam(
                        timeMode, nam, thang, tuan, quy, ngay, hopDongId, loaiHopDongId))
                .toEntity();
    }

    @GetMapping("/theo-linh-vuc")
    @Operation(summary = "Thống kê sản lượng theo lĩnh vực (loại HĐ) từng trung tâm; optional hopDongId")
    public ResponseEntity<ApiResponse<DashboardTheoLinhVucResponse>> theoLinhVuc(
            @RequestParam(required = false, defaultValue = "month") String timeMode,
            @RequestParam(required = false) Integer nam,
            @RequestParam(required = false) Integer thang,
            @RequestParam(required = false) Integer tuan,
            @RequestParam(required = false) Integer quy,
            @RequestParam(required = false) String ngay,
            @RequestParam(required = false) String hopDongId) {
        return ApiResponse.<DashboardTheoLinhVucResponse>build()
                .withData(dashboardService.theoLinhVuc(timeMode, nam, thang, tuan, quy, ngay, hopDongId))
                .toEntity();
    }
}
