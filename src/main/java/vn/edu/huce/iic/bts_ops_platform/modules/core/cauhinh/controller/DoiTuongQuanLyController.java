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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongQuanLyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doi-tuong-quan-ly")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.DoiTuongQuanLy.TAG, description = ApiEntityCatalog.DoiTuongQuanLy.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_DOI_TUONG_QUAN_LY)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class DoiTuongQuanLyController {

    private final DoiTuongQuanLyService doiTuongQuanLyService;

    @GetMapping
    @Operation(
            summary = "Danh sách Cấu hình — Đối tượng quản lý",
            description = """
                    Mỗi bản ghi trả về field `hienThiTrenGiaoDien` (boolean, mặc định true).
                    - `hienThiTrenGiaoDien=false`: ẩn khỏi dropdown import HĐ, vẫn dùng ở Mapping Excel.
                    - `selectorOnly=true`: lọc bỏ các bản ghi có `hienThiTrenGiaoDien=false`.
                    - Không truyền `selectorOnly`: trả danh sách đầy đủ (dùng cho Mapping Excel).
                    """
    )
    public ResponseEntity<ApiResponse<List<DoiTuongQuanLyResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID kieuHopDongId,
            @RequestParam(required = false) Boolean selectorOnly
    ) {
        return ApiResponse.<List<DoiTuongQuanLyResponse>>build()
                .withData(doiTuongQuanLyService.list(search, activeOnly, includeDeleted, loaiHopDongId, kieuHopDongId, selectorOnly))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<DoiTuongQuanLyResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<DoiTuongQuanLyResponse>build()
                .withData(doiTuongQuanLyService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<DoiTuongQuanLyResponse>> create(@Valid @RequestBody DoiTuongQuanLyTaoRequest request) {
        return ApiResponse.<DoiTuongQuanLyResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(doiTuongQuanLyService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<DoiTuongQuanLyResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTuongQuanLyCapNhatRequest request) {
        return ApiResponse.<DoiTuongQuanLyResponse>build()
                .withData(doiTuongQuanLyService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        doiTuongQuanLyService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
