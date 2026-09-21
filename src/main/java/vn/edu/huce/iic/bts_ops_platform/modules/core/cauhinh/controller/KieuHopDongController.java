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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kieu-hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.KieuHopDong.TAG, description = ApiEntityCatalog.KieuHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_KIEU_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class KieuHopDongController {

    private final KieuHopDongService kieuHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách Cấu hình — Kiểu hợp đồng")
    public ResponseEntity<ApiResponse<List<KieuHopDongResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID loaiHopDongId
    ) {
        return ApiResponse.<List<KieuHopDongResponse>>build()
                .withData(kieuHopDongService.list(search, activeOnly, includeDeleted, loaiHopDongId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<KieuHopDongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<KieuHopDongResponse>build()
                .withData(kieuHopDongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<KieuHopDongResponse>> create(@Valid @RequestBody KieuHopDongTaoRequest request) {
        return ApiResponse.<KieuHopDongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(kieuHopDongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<KieuHopDongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody KieuHopDongCapNhatRequest request) {
        return ApiResponse.<KieuHopDongResponse>build()
                .withData(kieuHopDongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

}
