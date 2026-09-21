package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thuoc-tinh")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ThuocTinh.TAG, description = ApiEntityCatalog.ThuocTinh.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_THUOC_TINH)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ThuocTinhController {

    private final ThuocTinhService thuocTinhService;

    @GetMapping
    @Operation(
            summary = "Danh sách Cấu hình — Thuộc tính",
            description = """
                    `selectorOnly=true`: bỏ thuộc tính thuộc đối tượng có `hienThiTrenGiaoDien=false`.
                    Không truyền `selectorOnly`: trả đủ thuộc tính (dùng cho Mapping Excel).
                    """
    )
    public ResponseEntity<ApiResponse<List<ThuocTinhResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @Parameter(
                    description = "true = bỏ thuộc tính của đối tượng ẩn selector (hienThiTrenGiaoDien=false). "
                            + "Bỏ qua = danh sách đầy đủ."
            )
            @RequestParam(required = false) Boolean selectorOnly) {
        return ApiResponse.<List<ThuocTinhResponse>>build()
                .withData(thuocTinhService.list(search, activeOnly, includeDeleted, doiTuongQuanLyId, selectorOnly))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<ThuocTinhResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<ThuocTinhResponse>build()
                .withData(thuocTinhService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<ThuocTinhResponse>> create(@Valid @RequestBody ThuocTinhTaoRequest request) {
        return ApiResponse.<ThuocTinhResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(thuocTinhService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<ThuocTinhResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ThuocTinhCapNhatRequest request) {
        return ApiResponse.<ThuocTinhResponse>build()
                .withData(thuocTinhService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        thuocTinhService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
