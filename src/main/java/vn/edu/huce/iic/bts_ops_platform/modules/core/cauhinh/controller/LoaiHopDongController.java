package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LoaiHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loai-hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.LoaiHopDong.TAG, description = ApiEntityCatalog.LoaiHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_LOAI_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class LoaiHopDongController {

    private final LoaiHopDongService loaiHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách Cấu hình — Loại hợp đồng")
    public ResponseEntity<ApiResponse<List<LoaiHopDongResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<LoaiHopDongResponse>>build()
                .withData(loaiHopDongService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<LoaiHopDongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<LoaiHopDongResponse>build()
                .withData(loaiHopDongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<LoaiHopDongResponse>> create(@Valid @RequestBody LoaiHopDongTaoRequest request) {
        return ApiResponse.<LoaiHopDongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(loaiHopDongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<LoaiHopDongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LoaiHopDongCapNhatRequest request) {
        return ApiResponse.<LoaiHopDongResponse>build()
                .withData(loaiHopDongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        loaiHopDongService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
