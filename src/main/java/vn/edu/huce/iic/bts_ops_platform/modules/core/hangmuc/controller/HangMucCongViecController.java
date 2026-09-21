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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucCongViecService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hang-muc-cong-viec")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HangMucCongViec.TAG, description = ApiEntityCatalog.HangMucCongViec.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HANG_MUC_CONG_VIEC)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HangMucCongViecController {

    private final HangMucCongViecService hangMucCongViecService;

    @GetMapping
    @Operation(summary = "Danh sách Hạng mục — Công việc")
    public ResponseEntity<ApiResponse<List<HangMucCongViecResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
    @RequestParam(required = false) UUID hangMucChiTietId) {
        return ApiResponse.<List<HangMucCongViecResponse>>build()
                .withData(hangMucCongViecService.list(search, activeOnly, includeDeleted, hangMucChiTietId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HangMucCongViecResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HangMucCongViecResponse>build()
                .withData(hangMucCongViecService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HangMucCongViecResponse>> create(@Valid @RequestBody HangMucCongViecTaoRequest request) {
        return ApiResponse.<HangMucCongViecResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hangMucCongViecService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HangMucCongViecResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HangMucCongViecCapNhatRequest request) {
        return ApiResponse.<HangMucCongViecResponse>build()
                .withData(hangMucCongViecService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hangMucCongViecService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
