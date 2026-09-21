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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomSaoChepRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucKhoiLuongSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomSaoChepResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hang-muc-nhom")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HangMucNhom.TAG, description = ApiEntityCatalog.HangMucNhom.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HANG_MUC_NHOM)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HangMucNhomController {

    private final HangMucNhomService hangMucNhomService;

    @GetMapping
    @Operation(summary = "Danh sách Hạng mục — Nhóm")
    public ResponseEntity<ApiResponse<List<HangMucNhomResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
    @RequestParam(required = false) UUID hopDongId) {
        return ApiResponse.<List<HangMucNhomResponse>>build()
                .withData(hangMucNhomService.list(search, activeOnly, includeDeleted, hopDongId))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}")
    @Operation(summary = "Danh sách nhóm hạng mục theo hợp đồng kèm KL sản lượng")
    public ResponseEntity<ApiResponse<HangMucNhomHopDongResponse>> listByHopDong(
            @PathVariable UUID hopDongId,
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<HangMucNhomHopDongResponse>build()
                .withData(hangMucNhomService.listByHopDongWithKhoiLuong(hopDongId, activeOnly))
                .toEntity();
    }

    @GetMapping("/cay-hop-dong")
    @Operation(summary = "Cây hạng mục thi công theo hợp đồng (nhóm → chi tiết → công việc + KL sản lượng)")
    public ResponseEntity<ApiResponse<HangMucHopDongTreeResponse>> getTreeByHopDong(
            @RequestParam UUID hopDongId,
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<HangMucHopDongTreeResponse>build()
                .withData(hangMucNhomService.getTreeByHopDongId(hopDongId, activeOnly))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HangMucNhomResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HangMucNhomResponse>build()
                .withData(hangMucNhomService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HangMucNhomResponse>> create(@Valid @RequestBody HangMucNhomTaoRequest request) {
        return ApiResponse.<HangMucNhomResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hangMucNhomService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HangMucNhomResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HangMucNhomCapNhatRequest request) {
        return ApiResponse.<HangMucNhomResponse>build()
                .withData(hangMucNhomService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hangMucNhomService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }

    @PostMapping("/sao-chep")
    @Operation(summary = "Sao chép toàn bộ hạng mục thi công từ hợp đồng nguồn sang hợp đồng đích")
    public ResponseEntity<ApiResponse<HangMucNhomSaoChepResponse>> copyFromHopDong(
            @Valid @RequestBody HangMucNhomSaoChepRequest request) {
        return ApiResponse.<HangMucNhomSaoChepResponse>build()
                .withData(hangMucNhomService.copyFromHopDong(request.getHopDongNguonId(), request.getHopDongDichId()))
                .withMessage("Đã sao chép hạng mục thi công")
                .toEntity();
    }
}
