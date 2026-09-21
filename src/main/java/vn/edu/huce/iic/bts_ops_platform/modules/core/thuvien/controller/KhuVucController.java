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
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.KhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.KhuVucService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/khu-vuc")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.KhuVuc.TAG, description = ApiEntityCatalog.KhuVuc.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_KHU_VUC)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class KhuVucController {

    private final KhuVucService khuVucService;

    @GetMapping
    @Operation(summary = "Danh sách khu vực")
    public ResponseEntity<ApiResponse<List<KhuVucResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<List<KhuVucResponse>>build()
                .withData(khuVucService.list(search, activeOnly))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết khu vực")
    public ResponseEntity<ApiResponse<KhuVucResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<KhuVucResponse>build()
                .withData(khuVucService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Thêm khu vực")
    public ResponseEntity<ApiResponse<KhuVucResponse>> create(@Valid @RequestBody KhuVucTaoRequest request) {
        return ApiResponse.<KhuVucResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(khuVucService.create(request))
                .withMessage("Đã thêm khu vực mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật khu vực")
    public ResponseEntity<ApiResponse<KhuVucResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody KhuVucCapNhatRequest request) {
        return ApiResponse.<KhuVucResponse>build()
                .withData(khuVucService.update(id, request))
                .withMessage("Đã cập nhật khu vực")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa khu vực (chỉ khi chưa gán tỉnh)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        khuVucService.delete(id);
        return ApiResponse.<Void>build()
                .withMessage("Đã xóa khu vực")
                .toEntity();
    }
}
