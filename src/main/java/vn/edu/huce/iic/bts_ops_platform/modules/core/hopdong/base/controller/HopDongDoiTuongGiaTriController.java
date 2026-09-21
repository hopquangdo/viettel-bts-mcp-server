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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong-doi-tuong-gia-tri")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongDoiTuongGiaTri.TAG, description = ApiEntityCatalog.HopDongDoiTuongGiaTri.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongDoiTuongGiaTriController {

    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HopDongDoiTuongGiaTriResponse>> create(@Valid @RequestBody HopDongDoiTuongGiaTriTaoRequest request) {
        return ApiResponse.<HopDongDoiTuongGiaTriResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongDoiTuongGiaTriService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HopDongDoiTuongGiaTriResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongDoiTuongGiaTriCapNhatRequest request) {
        return ApiResponse.<HopDongDoiTuongGiaTriResponse>build()
                .withData(hopDongDoiTuongGiaTriService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hopDongDoiTuongGiaTriService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
