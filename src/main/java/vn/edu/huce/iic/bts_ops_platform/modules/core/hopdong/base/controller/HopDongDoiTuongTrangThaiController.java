package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong-doi-tuong-trang-thai")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongDoiTuongTrangThai.TAG, description = ApiEntityCatalog.HopDongDoiTuongTrangThai.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_TRANG_THAI)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongDoiTuongTrangThaiController {

    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;

    @GetMapping
    @Operation(summary = "Danh sách HĐ — TT đối tượng")
    public ResponseEntity<ApiResponse<List<HopDongDoiTuongTrangThaiResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
    @RequestParam(required = false) UUID doiTuongId,
    @RequestParam(required = false) UUID kieuHopDongId,
    @RequestParam(required = false) UUID loaiHopDongId) {
        return ApiResponse.<List<HopDongDoiTuongTrangThaiResponse>>build()
                .withData(hopDongDoiTuongTrangThaiService.list(
                        search, activeOnly, includeDeleted, doiTuongId, kieuHopDongId, loaiHopDongId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HopDongDoiTuongTrangThaiResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HopDongDoiTuongTrangThaiResponse>build()
                .withData(hopDongDoiTuongTrangThaiService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HopDongDoiTuongTrangThaiResponse>> create(@Valid @RequestBody HopDongDoiTuongTrangThaiTaoRequest request) {
        return ApiResponse.<HopDongDoiTuongTrangThaiResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongDoiTuongTrangThaiService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HopDongDoiTuongTrangThaiResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongDoiTuongTrangThaiCapNhatRequest request) {
        return ApiResponse.<HopDongDoiTuongTrangThaiResponse>build()
                .withData(hopDongDoiTuongTrangThaiService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hopDongDoiTuongTrangThaiService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
