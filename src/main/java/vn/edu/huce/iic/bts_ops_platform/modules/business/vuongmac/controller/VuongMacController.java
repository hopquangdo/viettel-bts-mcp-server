package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTrungKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacDoiTuongOptionResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vuong-mac")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.VuongMac.TAG, description = ApiEntityCatalog.VuongMac.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_VUONG_MAC)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class VuongMacController {

    private final VuongMacService vuongMacService;

    @GetMapping
    @Operation(summary = "Danh sách Vướng mắc")
    public ResponseEntity<ApiResponse<List<VuongMacResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) String kieuVuongMac,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) UUID tinhThanhId
    ) {
        return ApiResponse.<List<VuongMacResponse>>build()
                .withData(vuongMacService.list(
                        search, activeOnly, includeDeleted, hopDongId, trangThai, giaiDoan, kieuVuongMac,
                        loaiHopDongId, khuVucId, tinhThanhId))
                .toEntity();
    }

    @GetMapping("/danh-sach")
    @Operation(summary = "Danh sách Vướng mắc (phân trang thật ở SQL, nhận cả search lẫn filter)")
    public ResponseEntity<ApiResponse<PageResponse<VuongMacResponse>>> listPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false, defaultValue = "false") boolean dangMoOnly,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) String kieuVuongMac,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) UUID tinhThanhId,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.<PageResponse<VuongMacResponse>>build()
                .withData(vuongMacService.listPage(
                        search, activeOnly, includeDeleted, hopDongId, trangThai, dangMoOnly, giaiDoan, kieuVuongMac,
                        loaiHopDongId, khuVucId, tinhThanhId, quaHanNgay, ngayBaoCao, dateFrom, dateTo, page, size))
                .toEntity();
    }

    @GetMapping("/tong-quan")
    @Operation(summary = "Tổng quan đếm theo trạng thái + quá hạn 30 ngày (SQL aggregate, dùng cho StatCard đầu trang Issues)")
    public ResponseEntity<ApiResponse<VuongMacTongQuanResponse>> tongQuan(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) String kieuVuongMac,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) UUID tinhThanhId,
            @RequestParam(required = false, defaultValue = "true") boolean includeKieuCounts
    ) {
        return ApiResponse.<VuongMacTongQuanResponse>build()
                .withData(vuongMacService.tongQuan(
                        activeOnly, includeDeleted, hopDongId, trangThai, giaiDoan, kieuVuongMac,
                        loaiHopDongId, khuVucId, tinhThanhId, includeKieuCounts))
                .toEntity();
    }

    @GetMapping("/doi-tuong-tim-kiem")
    @Operation(summary = "Tìm đối tượng HĐ để ghi nhận vướng mắc")
    public ResponseEntity<ApiResponse<List<VuongMacDoiTuongOptionResponse>>> timDoiTuong(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return ApiResponse.<List<VuongMacDoiTuongOptionResponse>>build()
                .withData(vuongMacService.timDoiTuong(search, hopDongId, limit))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết vướng mắc theo id")
    public ResponseEntity<ApiResponse<VuongMacResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<VuongMacResponse>build()
                .withData(vuongMacService.getById(id))
                .toEntity();
    }

    @GetMapping("/{id}/lich-su")
    @Operation(summary = "Lịch sử xử lý vướng mắc — append-only, mới nhất trước")
    public ResponseEntity<ApiResponse<List<VuongMacLichSuResponse>>> listLichSu(@PathVariable UUID id) {
        return ApiResponse.<List<VuongMacLichSuResponse>>build()
                .withData(vuongMacService.listLichSu(id))
                .toEntity();
    }

    @PostMapping("/kiem-tra-trung")
    @Operation(summary = "Kiểm tra vướng mắc trùng đối tượng + giai đoạn trước khi tạo mới")
    public ResponseEntity<ApiResponse<List<VuongMacResponse>>> kiemTraTrung(
            @Valid @RequestBody VuongMacTrungKiemTraRequest request) {
        return ApiResponse.<List<VuongMacResponse>>build()
                .withData(vuongMacService.kiemTraTrung(request))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<VuongMacResponse>> create(@Valid @RequestBody VuongMacTaoRequest request) {
        return ApiResponse.<VuongMacResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(vuongMacService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<VuongMacResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VuongMacCapNhatRequest request) {
        return ApiResponse.<VuongMacResponse>build()
                .withData(vuongMacService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @PostMapping(value = "/{id}/anh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh đính kèm vướng mắc")
    public ResponseEntity<ApiResponse<VuongMacResponse>> uploadAnh(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<VuongMacResponse>build()
                .withData(vuongMacService.uploadAnh(id, file))
                .withMessage("Đã tải ảnh lên")
                .toEntity();
    }

}
