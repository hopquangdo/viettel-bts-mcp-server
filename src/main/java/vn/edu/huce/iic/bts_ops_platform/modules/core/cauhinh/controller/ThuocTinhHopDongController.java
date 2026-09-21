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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thuoc-tinh-hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ThuocTinhHopDong.TAG, description = ApiEntityCatalog.ThuocTinhHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_THUOC_TINH)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ThuocTinhHopDongController {

    private final ThuocTinhHopDongService thuocTinhHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách thuộc tính thông tin HĐ")
    public ResponseEntity<ApiResponse<List<ThuocTinhHopDongResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<ThuocTinhHopDongResponse>>build()
                .withData(thuocTinhHopDongService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết thuộc tính HĐ")
    public ResponseEntity<ApiResponse<ThuocTinhHopDongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<ThuocTinhHopDongResponse>build()
                .withData(thuocTinhHopDongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo thuộc tính HĐ")
    public ResponseEntity<ApiResponse<ThuocTinhHopDongResponse>> create(
            @Valid @RequestBody ThuocTinhHopDongTaoRequest request) {
        return ApiResponse.<ThuocTinhHopDongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(thuocTinhHopDongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thuộc tính HĐ")
    public ResponseEntity<ApiResponse<ThuocTinhHopDongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ThuocTinhHopDongCapNhatRequest request) {
        return ApiResponse.<ThuocTinhHopDongResponse>build()
                .withData(thuocTinhHopDongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm thuộc tính HĐ")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        thuocTinhHopDongService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
