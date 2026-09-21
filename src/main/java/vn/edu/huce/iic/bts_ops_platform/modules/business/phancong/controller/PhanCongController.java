package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.controller;

import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.*;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.PhanCongDashboardService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.PhanCongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/phan-cong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.PhanCong.TAG, description = ApiEntityCatalog.PhanCong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_PHAN_CONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class PhanCongController {

    private final PhanCongService phanCongService;
    private final PhanCongDashboardService phanCongDashboardService;

    @GetMapping
    @Operation(summary = "Danh sách phân công")
    public ResponseEntity<ApiResponse<List<PhanCongResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId) {
        return ApiResponse.<List<PhanCongResponse>>build()
                .withData(phanCongService.list(search, activeOnly, includeDeleted, hopDongId))
                .toEntity();
    }

    @GetMapping("/thong-ke")
    @Operation(summary = "Thống kê phân công (legacy)")
    public ResponseEntity<ApiResponse<PhanCongThongKeResponse>> thongKe() {
        return ApiResponse.<PhanCongThongKeResponse>build()
                .withData(phanCongService.thongKe())
                .toEntity();
    }

    @GetMapping("/bo-loc")
    @Operation(summary = "Tùy chọn bộ lọc phân công")
    public ResponseEntity<ApiResponse<PhanCongBoLocResponse>> boLoc() {
        return ApiResponse.<PhanCongBoLocResponse>build()
                .withData(phanCongDashboardService.boLoc())
                .toEntity();
    }

    @GetMapping("/tong-quan")
    @Operation(summary = "Tổng quan phân công theo nhà thầu")
    public ResponseEntity<ApiResponse<PhanCongTongQuanResponse>> tongQuan(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) String search) {
        return ApiResponse.<PhanCongTongQuanResponse>build()
                .withData(phanCongDashboardService.tongQuan(hopDongId, nhaThauId, giaiDoan, khuVucId, search))
                .toEntity();
    }

    @GetMapping("/theo-nha-thau")
    @Operation(summary = "Tổng hợp theo nhà thầu")
    public ResponseEntity<ApiResponse<List<PhanCongNhaThauItemResponse>>> theoNhaThau(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) String search) {
        return ApiResponse.<List<PhanCongNhaThauItemResponse>>build()
                .withData(phanCongDashboardService.theoNhaThau(hopDongId, nhaThauId, giaiDoan, khuVucId, search))
                .toEntity();
    }

    @GetMapping("/theo-giai-doan")
    @Operation(summary = "Tổng hợp theo giai đoạn (trạng thái trạm)")
    public ResponseEntity<ApiResponse<List<PhanCongGiaiDoanItemResponse>>> theoGiaiDoan(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) String search) {
        return ApiResponse.<List<PhanCongGiaiDoanItemResponse>>build()
                .withData(phanCongDashboardService.theoGiaiDoan(hopDongId, nhaThauId, giaiDoan, khuVucId, search))
                .toEntity();
    }

    @GetMapping("/theo-ma-vung")
    @Operation(summary = "Tổng hợp theo mã vùng")
    public ResponseEntity<ApiResponse<List<PhanCongMaVungItemResponse>>> theoMaVung(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) String search) {
        return ApiResponse.<List<PhanCongMaVungItemResponse>>build()
                .withData(phanCongDashboardService.theoMaVung(hopDongId, nhaThauId, giaiDoan, khuVucId, search))
                .toEntity();
    }

    @GetMapping("/danh-sach-tram")
    @Operation(summary = "Danh sách trạm được phân công (phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<PhanCongTramItemResponse>>> danhSachTram(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<PhanCongTramItemResponse>>build()
                .withData(phanCongDashboardService.danhSachTram(
                        hopDongId, nhaThauId, giaiDoan, khuVucId, search, page, size))
                .toEntity();
    }

    @GetMapping("/lich-su")
    @Operation(summary = "Lịch sử phân công nhà thầu (lưu vĩnh viễn)")
    public ResponseEntity<ApiResponse<PageResponse<PhanCongLichSuResponse>>> lichSu(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID hopDongDoiTuongId,
            @RequestParam(required = false) UUID nhaThauId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<PhanCongLichSuResponse>>build()
                .withData(phanCongDashboardService.lichSu(hopDongId, hopDongDoiTuongId, nhaThauId, page, size))
                .toEntity();
    }

    @PostMapping("/gan")
    @Operation(summary = "Gán / đổi nhà thầu cho danh sách trạm")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> gan(@Valid @RequestBody PhanCongGanRequest request) {
        int updated = phanCongDashboardService.ganNhaThau(request);
        return ApiResponse.<Map<String, Integer>>build()
                .withData(Map.of("updated", updated))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo bản ghi phân công")
    public ResponseEntity<ApiResponse<PhanCongResponse>> create(@Valid @RequestBody PhanCongTaoRequest request) {
        return ApiResponse.<PhanCongResponse>build()
                .withData(phanCongService.create(request))
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phân công")
    public ResponseEntity<ApiResponse<PhanCongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PhanCongCapNhatRequest request) {
        return ApiResponse.<PhanCongResponse>build()
                .withData(phanCongService.update(id, request))
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm phân công")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        phanCongService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
