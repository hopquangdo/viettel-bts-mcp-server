package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucChiTietService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hang-muc-chi-tiet")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HangMucChiTiet.TAG, description = ApiEntityCatalog.HangMucChiTiet.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HANG_MUC_CHI_TIET)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HangMucChiTietController {

    private final HangMucChiTietService hangMucChiTietService;

    @GetMapping
    @Operation(summary = "Danh sách Hạng mục — Chi tiết")
    public ResponseEntity<ApiResponse<List<HangMucChiTietResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
    @RequestParam(required = false) UUID hangMucNhomId) {
        return ApiResponse.<List<HangMucChiTietResponse>>build()
                .withData(hangMucChiTietService.list(search, activeOnly, includeDeleted, hangMucNhomId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HangMucChiTietResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HangMucChiTietResponse>build()
                .withData(hangMucChiTietService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HangMucChiTietResponse>> create(@Valid @RequestBody HangMucChiTietTaoRequest request) {
        return ApiResponse.<HangMucChiTietResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hangMucChiTietService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HangMucChiTietResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HangMucChiTietCapNhatRequest request) {
        return ApiResponse.<HangMucChiTietResponse>build()
                .withData(hangMucChiTietService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hangMucChiTietService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
