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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienGanDoiTuongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongNhomUuTienResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongNhomUuTienService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong-nhom-uu-tien")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongNhomUuTien.TAG, description = ApiEntityCatalog.HopDongNhomUuTien.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_NHOM_UU_TIEN)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongNhomUuTienController {

    private final HopDongNhomUuTienService hopDongNhomUuTienService;
    private final HopDongDoiTuongService hopDongDoiTuongService;

    @GetMapping
    @Operation(summary = "Danh sách HĐ — Nhóm ưu tiên")
    public ResponseEntity<ApiResponse<List<HopDongNhomUuTienResponse>>> list(
            @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
    @RequestParam(required = false) UUID hopDongId) {
        return ApiResponse.<List<HopDongNhomUuTienResponse>>build()
                .withData(hopDongNhomUuTienService.list(search, activeOnly, includeDeleted, hopDongId))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HopDongNhomUuTienResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HopDongNhomUuTienResponse>build()
                .withData(hopDongNhomUuTienService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HopDongNhomUuTienResponse>> create(@Valid @RequestBody HopDongNhomUuTienTaoRequest request) {
        return ApiResponse.<HopDongNhomUuTienResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongNhomUuTienService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HopDongNhomUuTienResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongNhomUuTienCapNhatRequest request) {
        return ApiResponse.<HopDongNhomUuTienResponse>build()
                .withData(hopDongNhomUuTienService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hopDongNhomUuTienService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }

    @PutMapping("/{id}/gan-doi-tuong")
    @Operation(summary = "Gắn đối tượng vào nhóm ưu tiên")
    public ResponseEntity<ApiResponse<List<HopDongNhomUuTienResponse>>> assignDoiTuong(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongNhomUuTienGanDoiTuongRequest request) {
        return ApiResponse.<List<HopDongNhomUuTienResponse>>build()
                .withData(hopDongNhomUuTienService.assignDoiTuong(id, request.getDoiTuongIds()))
                .withMessage("Đã gắn đối tượng vào nhóm ưu tiên")
                .toEntity();
    }

    @GetMapping("/{id}/doi-tuong")
    @Operation(summary = "Danh sách đối tượng thuộc nhóm ưu tiên")
    public ResponseEntity<ApiResponse<List<HopDongDoiTuongResponse>>> listDoiTuong(@PathVariable UUID id) {
        hopDongNhomUuTienService.getById(id);
        return ApiResponse.<List<HopDongDoiTuongResponse>>build()
                .withData(hopDongDoiTuongService.listByNhomUuTien(id))
                .toEntity();
    }
}
