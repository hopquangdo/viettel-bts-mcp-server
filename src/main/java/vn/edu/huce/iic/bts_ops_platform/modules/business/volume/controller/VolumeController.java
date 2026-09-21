package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeBoSungRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeCauHinhNguongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCauHinhNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanDotRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeHopDongRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeProvinceRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeQuyetToanTongHopResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.services.VolumeService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongQuyetToanDotService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/volume")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.Volume.TAG, description = ApiEntityCatalog.Volume.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_LUONG_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class VolumeController {

    private final VolumeService volumeService;
    private final HopDongDoiTuongQuyetToanDotService quyetToanDotService;
    private final ObjectMapper objectMapper;

    @GetMapping("/cau-hinh-nguong")
    @Operation(summary = "Đọc cấu hình hệ số ngưỡng cảnh báo Volume (GCCC, Xây mới, override theo HĐ)")
    public ResponseEntity<ApiResponse<VolumeCauHinhNguongResponse>> getCauHinhNguong() {
        return ApiResponse.<VolumeCauHinhNguongResponse>build()
                .withData(volumeService.getCauHinhNguong())
                .toEntity();
    }

    @PutMapping("/cau-hinh-nguong")
    @Operation(summary = "Lưu cấu hình hệ số ngưỡng — áp dụng chung toàn hệ thống")
    public ResponseEntity<ApiResponse<VolumeCauHinhNguongResponse>> saveCauHinhNguong(
            @Valid @RequestBody VolumeCauHinhNguongRequest request) {
        return ApiResponse.<VolumeCauHinhNguongResponse>build()
                .withData(volumeService.saveCauHinhNguong(request))
                .toEntity();
    }

    @GetMapping("/tong-quan")
    @Operation(summary = "Tổng quan đối soát giá trị HĐ vs thành tiền hạng mục (KL sản lượng)")
    public ResponseEntity<ApiResponse<VolumeTongQuanResponse>> tongQuan(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String heSoOverrides) {
        return ApiResponse.<VolumeTongQuanResponse>build()
                .withData(volumeService.tongQuan(loaiHopDongId, search, heSo, parseHeSoOverrides(heSoOverrides)))
                .toEntity();
    }

    @GetMapping("/hop-dong")
    @Operation(summary = "Danh sách hợp đồng kèm giá trị HĐ và thành tiền thi công (không phân trang)")
    public ResponseEntity<ApiResponse<List<VolumeHopDongRowResponse>>> listHopDong(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String heSoOverrides) {
        return ApiResponse.<List<VolumeHopDongRowResponse>>build()
                .withData(volumeService.listHopDong(loaiHopDongId, search, heSo, parseHeSoOverrides(heSoOverrides)))
                .toEntity();
    }

    @GetMapping("/hop-dong/danh-sach")
    @Operation(summary = "Danh sách hợp đồng phân trang (bảng chính trang Kiểm soát Volume)")
    public ResponseEntity<ApiResponse<PageResponse<VolumeHopDongRowResponse>>> danhSachHopDong(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String heSoOverrides,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<VolumeHopDongRowResponse>>build()
                .withData(volumeService.danhSach(
                        loaiHopDongId, search, heSo, parseHeSoOverrides(heSoOverrides), statusFilter, page, size))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}")
    @Operation(summary = "Chi tiết đối soát 1 hợp đồng (breakdown theo nhóm hạng mục)")
    public ResponseEntity<ApiResponse<VolumeChiTietResponse>> getChiTiet(@PathVariable UUID hopDongId) {
        return ApiResponse.<VolumeChiTietResponse>build()
                .withData(volumeService.getChiTiet(hopDongId))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/khu-vuc")
    @Operation(summary = "Volume theo khu vực / tỉnh của 1 hợp đồng (lazy expand)")
    public ResponseEntity<ApiResponse<VolumeKhuVucResponse>> khuVucByHopDong(
            @PathVariable UUID hopDongId,
            @RequestParam(required = false) BigDecimal heSo) {
        return ApiResponse.<VolumeKhuVucResponse>build()
                .withData(volumeService.khuVucByHopDong(hopDongId, heSo))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/khu-vuc/{regionId}/tinh")
    @Operation(summary = "Danh sách tỉnh phân trang của 1 khu vực trong hợp đồng (lazy drill-in)")
    public ResponseEntity<ApiResponse<PageResponse<VolumeProvinceRow>>> danhSachTinh(
            @PathVariable UUID hopDongId,
            @PathVariable String regionId,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String variantFilter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String groupBy) {
        return ApiResponse.<PageResponse<VolumeProvinceRow>>build()
                .withData(volumeService.danhSachTinh(hopDongId, regionId, heSo, variantFilter, page, size, groupBy))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/khu-vuc/{regionId}/tinh/dem")
    @Operation(summary = "Đếm tỉnh theo tab lọc (all/shortage/surplus/normal) — cho tab bar")
    public ResponseEntity<ApiResponse<Map<String, Long>>> demTinh(
            @PathVariable UUID hopDongId,
            @PathVariable String regionId,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String groupBy) {
        return ApiResponse.<Map<String, Long>>build()
                .withData(volumeService.demTinh(hopDongId, regionId, heSo, groupBy))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tinh/{provinceKey}/tram/danh-sach")
    @Operation(summary = "Danh sách trạm phân trang theo tỉnh trong hợp đồng (lazy drill-in)")
    public ResponseEntity<ApiResponse<PageResponse<VolumeTramRow>>> danhSachTram(
            @PathVariable UUID hopDongId,
            @PathVariable String provinceKey,
            @RequestParam(required = false) BigDecimal heSo,
            @RequestParam(required = false) String variantFilter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<VolumeTramRow>>build()
                .withData(volumeService.danhSachTram(hopDongId, provinceKey, heSo, variantFilter, page, size))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tinh/{provinceKey}/tram/dem")
    @Operation(summary = "Đếm trạm theo tab lọc (all/alert/abnormal/shortage/ok) — cho tab bar")
    public ResponseEntity<ApiResponse<Map<String, Long>>> demTram(
            @PathVariable UUID hopDongId,
            @PathVariable String provinceKey,
            @RequestParam(required = false) BigDecimal heSo) {
        return ApiResponse.<Map<String, Long>>build()
                .withData(volumeService.demTram(hopDongId, provinceKey, heSo))
                .toEntity();
    }

    @PutMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/quyet-toan")
    @Operation(summary = "Cập nhật quyết toán thực theo trạm (đối tượng HĐ)")
    public ResponseEntity<ApiResponse<VolumeTramRow>> updateQuyetToan(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @RequestBody VolumeQuyetToanRequest request,
            @RequestParam(required = false) BigDecimal heSo) {
        return ApiResponse.<VolumeTramRow>build()
                .withData(volumeService.updateQuyetToan(hopDongId, hopDongDoiTuongId, request, heSo))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/quyet-toan-dot")
    @Operation(summary = "Danh sách đợt quyết toán + tổng quyết toán của 1 trạm")
    public ResponseEntity<ApiResponse<VolumeQuyetToanTongHopResponse>> listQuyetToanDot(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId) {
        return ApiResponse.<VolumeQuyetToanTongHopResponse>build()
                .withData(quyetToanDotService.list(hopDongId, hopDongDoiTuongId))
                .toEntity();
    }

    @PostMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/quyet-toan-dot")
    @Operation(summary = "Thêm 1 đợt quyết toán cho trạm — tổng quyết toán tự cộng dồn lại")
    public ResponseEntity<ApiResponse<VolumeQuyetToanTongHopResponse>> createQuyetToanDot(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @RequestBody VolumeQuyetToanDotRequest request) {
        return ApiResponse.<VolumeQuyetToanTongHopResponse>build()
                .withData(quyetToanDotService.create(hopDongId, hopDongDoiTuongId, request))
                .toEntity();
    }

    @PutMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/quyet-toan-dot/{dotId}")
    @Operation(summary = "Sửa 1 đợt quyết toán — tổng quyết toán tự tính lại")
    public ResponseEntity<ApiResponse<VolumeQuyetToanTongHopResponse>> updateQuyetToanDot(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @PathVariable UUID dotId,
            @RequestBody VolumeQuyetToanDotRequest request) {
        return ApiResponse.<VolumeQuyetToanTongHopResponse>build()
                .withData(quyetToanDotService.update(hopDongId, hopDongDoiTuongId, dotId, request))
                .toEntity();
    }

    @DeleteMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/quyet-toan-dot/{dotId}")
    @Operation(summary = "Xóa 1 đợt quyết toán — tổng quyết toán tự tính lại")
    public ResponseEntity<ApiResponse<VolumeQuyetToanTongHopResponse>> deleteQuyetToanDot(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @PathVariable UUID dotId) {
        return ApiResponse.<VolumeQuyetToanTongHopResponse>build()
                .withData(quyetToanDotService.delete(hopDongId, hopDongDoiTuongId, dotId))
                .toEntity();
    }

    @PutMapping("/hop-dong/{hopDongId}/tram/{hopDongDoiTuongId}/bo-sung")
    @Operation(summary = "Bổ sung / giảm sản lượng theo trạm — cập nhật CL hợp đồng")
    public ResponseEntity<ApiResponse<VolumeTramRow>> updateBoSung(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @RequestBody VolumeBoSungRequest request,
            @RequestParam(required = false) BigDecimal heSo) {
        return ApiResponse.<VolumeTramRow>build()
                .withData(volumeService.updateBoSungSanLuong(hopDongId, hopDongDoiTuongId, request, heSo))
                .toEntity();
    }

    /** JSON object: { "hopDongId": heSo, ... } */
    private Map<UUID, BigDecimal> parseHeSoOverrides(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, BigDecimal> parsed = objectMapper.readValue(raw, new TypeReference<>() {});
            Map<UUID, BigDecimal> result = new HashMap<>();
            for (Map.Entry<String, BigDecimal> e : parsed.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                if (e.getValue().compareTo(BigDecimal.ZERO) <= 0) continue;
                result.put(UUID.fromString(e.getKey()), e.getValue());
            }
            return result;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }
}
