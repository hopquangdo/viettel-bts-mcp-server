package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingCotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/excel-mapping")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ExcelMapping.TAG, description = ApiEntityCatalog.ExcelMapping.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_EXCEL_MAPPING)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ExcelMappingController {

    private final ExcelMappingService excelMappingService;

    @GetMapping
    @Operation(summary = "Danh sách Excel mapping")
    @RequiresPermission(anyOf = {QuyenHanMa.QUAN_LY_EXCEL_MAPPING, QuyenHanMa.QUAN_LY_HOP_DONG})
    public ResponseEntity<ApiResponse<List<ExcelMappingResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID doiTuongQuanLyId
    ) {
        return ApiResponse.<List<ExcelMappingResponse>>build()
                .withData(excelMappingService.list(search, activeOnly, includeDeleted, doiTuongQuanLyId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết Excel mapping")
    @RequiresPermission(anyOf = {QuyenHanMa.QUAN_LY_EXCEL_MAPPING, QuyenHanMa.QUAN_LY_HOP_DONG})
    public ResponseEntity<ApiResponse<ExcelMappingResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<ExcelMappingResponse>build()
                .withData(excelMappingService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới Excel mapping")
    public ResponseEntity<ApiResponse<ExcelMappingResponse>> create(@Valid @RequestBody ExcelMappingTaoRequest request) {
        return ApiResponse.<ExcelMappingResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(excelMappingService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật Excel mapping")
    public ResponseEntity<ApiResponse<ExcelMappingResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExcelMappingCapNhatRequest request) {
        return ApiResponse.<ExcelMappingResponse>build()
                .withData(excelMappingService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @PutMapping("/{id}/dong-bo")
    @Operation(summary = "Đồng bộ cột Excel")
    public ResponseEntity<ApiResponse<List<ExcelMappingCotResponse>>> dongBo(
            @PathVariable UUID id,
            @Valid @RequestBody ExcelMappingDongBoRequest request) {
        return ApiResponse.<List<ExcelMappingCotResponse>>build()
                .withData(excelMappingService.dongBo(id, request))
                .withMessage("Đã đồng bộ cột")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm Excel mapping")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        excelMappingService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
