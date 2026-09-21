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
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenHanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.QuyenHanService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quyen-han")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.QuyenHan.TAG, description = ApiEntityCatalog.QuyenHan.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_QUYEN_HAN)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class QuyenHanController {

    private final QuyenHanService quyenHanService;

    @GetMapping
    @Operation(summary = "Danh sách Phân quyền — Quyền hạn")
    public ResponseEntity<ApiResponse<List<QuyenHanResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<QuyenHanResponse>>build()
                .withData(quyenHanService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo quyền hạn mới (seed thư viện quyền qua API thay vì SQL tay)")
    public ResponseEntity<ApiResponse<QuyenHanResponse>> create(
            @Valid @RequestBody QuyenHanTaoRequest request) {
        return ApiResponse.<QuyenHanResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(quyenHanService.create(request))
                .withMessage("Đã tạo quyền hạn")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật quyền hạn")
    public ResponseEntity<ApiResponse<QuyenHanResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody QuyenHanCapNhatRequest request) {
        return ApiResponse.<QuyenHanResponse>build()
                .withData(quyenHanService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }
}
