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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuDuLieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuDuLieuService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kieu-du-lieu")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.KieuDuLieu.TAG, description = ApiEntityCatalog.KieuDuLieu.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_KIEU_DU_LIEU)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class KieuDuLieuController {

    private final KieuDuLieuService kieuDuLieuService;

    @GetMapping
    @Operation(summary = "Danh sách Cấu hình — Kiểu dữ liệu")
    public ResponseEntity<ApiResponse<List<KieuDuLieuResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<KieuDuLieuResponse>>build()
                .withData(kieuDuLieuService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

}
