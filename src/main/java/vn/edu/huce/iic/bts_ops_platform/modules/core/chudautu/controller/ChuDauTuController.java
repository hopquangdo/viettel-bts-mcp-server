package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.response.ChuDauTuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.services.ChuDauTuService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chu-dau-tu")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ChuDauTu.TAG, description = ApiEntityCatalog.ChuDauTu.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_CHU_DAU_TU)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ChuDauTuController {

    private final ChuDauTuService chuDauTuService;

    @GetMapping
    @Operation(summary = "Danh sách Hợp đồng — Chủ đầu tư")
    public ResponseEntity<ApiResponse<List<ChuDauTuResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<ChuDauTuResponse>>build()
                .withData(chuDauTuService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<ChuDauTuResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<ChuDauTuResponse>build()
                .withData(chuDauTuService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<ChuDauTuResponse>> create(@Valid @RequestBody ChuDauTuTaoRequest request) {
        return ApiResponse.<ChuDauTuResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(chuDauTuService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<ChuDauTuResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ChuDauTuCapNhatRequest request) {
        return ApiResponse.<ChuDauTuResponse>build()
                .withData(chuDauTuService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        chuDauTuService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
