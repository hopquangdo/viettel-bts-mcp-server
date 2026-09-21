package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.TinhThanhService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tinh-thanh")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.TinhThanh.TAG, description = ApiEntityCatalog.TinhThanh.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_TINH_THANH)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class TinhThanhController {

    private final TinhThanhService tinhThanhService;

    @GetMapping
    @Operation(summary = "Danh sách tỉnh/thành")
    public ResponseEntity<ApiResponse<List<TinhThanhResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID khuVucId,
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<List<TinhThanhResponse>>build()
                .withData(tinhThanhService.list(search, khuVucId, activeOnly))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Thêm tỉnh/thành")
    public ResponseEntity<ApiResponse<TinhThanhResponse>> create(@Valid @RequestBody TinhThanhTaoRequest request) {
        return ApiResponse.<TinhThanhResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(tinhThanhService.create(request))
                .withMessage("Đã thêm tỉnh/thành")
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tỉnh/thành")
    public ResponseEntity<ApiResponse<TinhThanhResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<TinhThanhResponse>build()
                .withData(tinhThanhService.getById(id))
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tỉnh/thành")
    public ResponseEntity<ApiResponse<TinhThanhResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TinhThanhCapNhatRequest request) {
        return ApiResponse.<TinhThanhResponse>build()
                .withData(tinhThanhService.update(id, request))
                .withMessage("Đã cập nhật tỉnh/thành")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm tỉnh/thành")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        tinhThanhService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa tỉnh/thành").toEntity();
    }

    // FE Quản lý tỉnh thao tác theo NHÓM sáp nhập (1 tỉnh hiện tại + các tỉnh cũ) — service
    // có sẵn createGroup/updateGroup/deleteGroup nhưng trước đây chưa được map endpoint nào,
    // nên mọi thao tác nhóm từ UI đều rơi vào 405 → bảng tỉnh không thể có dữ liệu.
    @PostMapping("/nhom")
    @Operation(summary = "Tạo nhóm tỉnh (tỉnh hiện tại + các tỉnh cũ sáp nhập)")
    public ResponseEntity<ApiResponse<TinhThanhNhomResponse>> createGroup(
            @Valid @RequestBody TinhThanhNhomTaoRequest request) {
        return ApiResponse.<TinhThanhNhomResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(tinhThanhService.createGroup(request))
                .withMessage("Đã tạo nhóm tỉnh/thành")
                .toEntity();
    }

    @PutMapping("/nhom/{tinhThanhId}")
    @Operation(summary = "Cập nhật nhóm tỉnh")
    public ResponseEntity<ApiResponse<TinhThanhNhomResponse>> updateGroup(
            @PathVariable UUID tinhThanhId,
            @Valid @RequestBody TinhThanhNhomCapNhatRequest request) {
        return ApiResponse.<TinhThanhNhomResponse>build()
                .withData(tinhThanhService.updateGroup(tinhThanhId, request))
                .withMessage("Đã cập nhật nhóm tỉnh/thành")
                .toEntity();
    }

    @DeleteMapping("/nhom/{tinhThanhId}")
    @Operation(summary = "Xóa nhóm tỉnh")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable UUID tinhThanhId) {
        tinhThanhService.deleteGroup(tinhThanhId);
        return ApiResponse.<Void>build().withMessage("Đã xóa nhóm tỉnh/thành").toEntity();
    }
}
