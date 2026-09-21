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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DanhMucBienBanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DanhMucBienBanService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/danh-muc-bien-ban")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.DanhMucBienBan.TAG, description = ApiEntityCatalog.DanhMucBienBan.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_KIEU_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class DanhMucBienBanController {

    private final DanhMucBienBanService danhMucBienBanService;

    @GetMapping
    @Operation(summary = "Danh sách danh mục biên bản")
    public ResponseEntity<ApiResponse<List<DanhMucBienBanResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<DanhMucBienBanResponse>>build()
                .withData(danhMucBienBanService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết danh mục biên bản")
    public ResponseEntity<ApiResponse<DanhMucBienBanResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<DanhMucBienBanResponse>build()
                .withData(danhMucBienBanService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo danh mục biên bản")
    public ResponseEntity<ApiResponse<DanhMucBienBanResponse>> create(
            @Valid @RequestBody DanhMucBienBanTaoRequest request) {
        return ApiResponse.<DanhMucBienBanResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(danhMucBienBanService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật danh mục biên bản")
    public ResponseEntity<ApiResponse<DanhMucBienBanResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DanhMucBienBanCapNhatRequest request) {
        return ApiResponse.<DanhMucBienBanResponse>build()
                .withData(danhMucBienBanService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm danh mục biên bản")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        danhMucBienBanService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
