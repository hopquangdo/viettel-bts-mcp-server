package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.QuyenService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quyen")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.Quyen.TAG, description = ApiEntityCatalog.Quyen.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_QUYEN)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class QuyenController {

    private final QuyenService quyenService;

    @GetMapping
    @Operation(summary = "Danh sách Phân quyền — Quyền")
    public ResponseEntity<ApiResponse<List<QuyenResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<QuyenResponse>>build()
                .withData(quyenService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo vai trò mới (seed vai trò 4 cấp qua API thay vì SQL tay)")
    public ResponseEntity<ApiResponse<QuyenResponse>> create(@Valid @RequestBody QuyenTaoRequest request) {
        return ApiResponse.<QuyenResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(quyenService.create(request))
                .withMessage("Đã tạo vai trò")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật vai trò (đổi tên hiển thị trên ma trận)")
    public ResponseEntity<ApiResponse<QuyenResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody QuyenCapNhatRequest request) {
        return ApiResponse.<QuyenResponse>build()
                .withData(quyenService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }
}
