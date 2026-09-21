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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongDanhMucBienBanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongDanhMucBienBanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongDanhMucBienBanService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kieu-hop-dong-danh-muc-bien-ban")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.KieuHopDongDanhMucBienBan.TAG, description = ApiEntityCatalog.KieuHopDongDanhMucBienBan.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_KIEU_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class KieuHopDongDanhMucBienBanController {

    private final KieuHopDongDanhMucBienBanService kieuHopDongDanhMucBienBanService;

    @GetMapping
    @Operation(summary = "Danh sách danh mục biên bản gắn với kiểu HĐ (checklist hồ sơ)")
    public ResponseEntity<ApiResponse<List<KieuHopDongDanhMucBienBanResponse>>> list(
            @RequestParam UUID kieuHopDongId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<KieuHopDongDanhMucBienBanResponse>>build()
                .withData(kieuHopDongDanhMucBienBanService.list(kieuHopDongId, activeOnly, includeDeleted))
                .toEntity();
    }

    @PutMapping("/dong-bo")
    @Operation(summary = "Đồng bộ danh mục biên bản cho kiểu HĐ")
    public ResponseEntity<ApiResponse<List<KieuHopDongDanhMucBienBanResponse>>> sync(
            @Valid @RequestBody KieuHopDongDanhMucBienBanDongBoRequest request) {
        return ApiResponse.<List<KieuHopDongDanhMucBienBanResponse>>build()
                .withData(kieuHopDongDanhMucBienBanService.sync(request))
                .withMessage("Đã cập nhật danh mục biên bản")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm liên kết danh mục biên bản — kiểu HĐ")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        kieuHopDongDanhMucBienBanService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
