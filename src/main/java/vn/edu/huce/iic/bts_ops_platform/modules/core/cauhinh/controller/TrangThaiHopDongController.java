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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trang-thai-hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.TrangThaiHopDong.TAG, description = ApiEntityCatalog.TrangThaiHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_TRANG_THAI_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class TrangThaiHopDongController {

    private final TrangThaiHopDongService trangThaiHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách Cấu hình — Trạng thái HĐ")
    public ResponseEntity<ApiResponse<List<TrangThaiHopDongResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<TrangThaiHopDongResponse>>build()
                .withData(trangThaiHopDongService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<TrangThaiHopDongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<TrangThaiHopDongResponse>build()
                .withData(trangThaiHopDongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<TrangThaiHopDongResponse>> create(@Valid @RequestBody TrangThaiHopDongTaoRequest request) {
        return ApiResponse.<TrangThaiHopDongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(trangThaiHopDongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<TrangThaiHopDongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TrangThaiHopDongCapNhatRequest request) {
        return ApiResponse.<TrangThaiHopDongResponse>build()
                .withData(trangThaiHopDongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

}
