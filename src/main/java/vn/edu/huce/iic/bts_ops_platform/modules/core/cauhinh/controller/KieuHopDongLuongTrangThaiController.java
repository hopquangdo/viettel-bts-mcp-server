package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongLuongTrangThaiGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongLuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongLuongTrangThaiService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kieu-hop-dong-luong-trang-thai")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.KieuHopDongLuongTrangThai.TAG, description = ApiEntityCatalog.KieuHopDongLuongTrangThai.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_TRANG_THAI)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class KieuHopDongLuongTrangThaiController {

    private final KieuHopDongLuongTrangThaiService service;

    @GetMapping
    @Operation(summary = "Danh sách gắn luồng trạng thái theo kiểu HĐ")
    public ResponseEntity<ApiResponse<List<KieuHopDongLuongTrangThaiResponse>>> list(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID kieuHopDongId) {
        return ApiResponse.<List<KieuHopDongLuongTrangThaiResponse>>build()
                .withData(service.list(loaiHopDongId, kieuHopDongId))
                .toEntity();
    }

    @PutMapping("/gan")
    @Operation(summary = "Gắn hoặc gỡ luồng trạng thái cho kiểu HĐ")
    public ResponseEntity<ApiResponse<KieuHopDongLuongTrangThaiResponse>> gan(
            @Valid @RequestBody KieuHopDongLuongTrangThaiGanRequest request) {
        return ApiResponse.<KieuHopDongLuongTrangThaiResponse>build()
                .withData(service.gan(request))
                .withMessage(request.getLuongTrangThaiId() == null ? "Đã gỡ luồng trạng thái" : "Đã gắn luồng trạng thái")
                .toEntity();
    }

}
