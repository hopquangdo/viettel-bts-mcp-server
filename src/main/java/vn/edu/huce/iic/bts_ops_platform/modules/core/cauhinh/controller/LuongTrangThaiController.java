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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LuongTrangThaiService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/luong-trang-thai")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.LuongTrangThai.TAG, description = ApiEntityCatalog.LuongTrangThai.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_TRANG_THAI_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class LuongTrangThaiController {

    private final LuongTrangThaiService luongTrangThaiService;

    @GetMapping
    @Operation(summary = "Danh sách luồng trạng thái")
    public ResponseEntity<ApiResponse<List<LuongTrangThaiResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<LuongTrangThaiResponse>>build()
                .withData(luongTrangThaiService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết luồng trạng thái")
    public ResponseEntity<ApiResponse<LuongTrangThaiResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<LuongTrangThaiResponse>build()
                .withData(luongTrangThaiService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo luồng trạng thái")
    public ResponseEntity<ApiResponse<LuongTrangThaiResponse>> create(
            @Valid @RequestBody LuongTrangThaiTaoRequest request) {
        return ApiResponse.<LuongTrangThaiResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(luongTrangThaiService.create(request))
                .withMessage("Đã tạo luồng trạng thái")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật luồng trạng thái")
    public ResponseEntity<ApiResponse<LuongTrangThaiResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LuongTrangThaiCapNhatRequest request) {
        return ApiResponse.<LuongTrangThaiResponse>build()
                .withData(luongTrangThaiService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @PutMapping("/{id}/dong-bo")
    @Operation(summary = "Đồng bộ các bước trạng thái trong luồng (kèm thứ tự)")
    public ResponseEntity<ApiResponse<LuongTrangThaiResponse>> syncBuoc(
            @PathVariable UUID id,
            @Valid @RequestBody LuongTrangThaiDongBoRequest request) {
        return ApiResponse.<LuongTrangThaiResponse>build()
                .withData(luongTrangThaiService.syncBuoc(id, request))
                .withMessage("Đã cập nhật các bước trong luồng")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm luồng trạng thái")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        luongTrangThaiService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
