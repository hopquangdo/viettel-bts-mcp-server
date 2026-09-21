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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhKieuHopDongDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhKieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhKieuHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thuoc-tinh-kieu-hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ThuocTinhKieuHopDong.TAG, description = ApiEntityCatalog.ThuocTinhKieuHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_THUOC_TINH)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ThuocTinhKieuHopDongController {

    private final ThuocTinhKieuHopDongService thuocTinhKieuHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách thuộc tính gắn với kiểu HĐ")
    public ResponseEntity<ApiResponse<List<ThuocTinhKieuHopDongResponse>>> list(
            @RequestParam UUID kieuHopDongId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<ThuocTinhKieuHopDongResponse>>build()
                .withData(thuocTinhKieuHopDongService.list(kieuHopDongId, activeOnly, includeDeleted))
                .toEntity();
    }

    @PutMapping("/dong-bo")
    @Operation(summary = "Đồng bộ thuộc tính cho kiểu HĐ")
    public ResponseEntity<ApiResponse<List<ThuocTinhKieuHopDongResponse>>> sync(
            @Valid @RequestBody ThuocTinhKieuHopDongDongBoRequest request) {
        return ApiResponse.<List<ThuocTinhKieuHopDongResponse>>build()
                .withData(thuocTinhKieuHopDongService.sync(request))
                .withMessage("Đã cập nhật liên kết thuộc tính")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm liên kết thuộc tính — kiểu HĐ")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        thuocTinhKieuHopDongService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
