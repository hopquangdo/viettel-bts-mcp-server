package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.NguoiDungCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.TaoTaiKhoanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungThamChieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.NguoiDungService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nguoi-dung")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.NguoiDung.TAG, description = ApiEntityCatalog.NguoiDung.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_NGUOI_DUNG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    @GetMapping
    @Operation(summary = "Danh sách người dùng")
    public ResponseEntity<ApiResponse<List<NguoiDungResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID khuVucId) {
        return ApiResponse.<List<NguoiDungResponse>>build()
                .withData(nguoiDungService.list(search, activeOnly, includeDeleted, khuVucId))
                .toEntity();
    }

    @GetMapping("/tham-chieu")
    @Operation(summary = "Danh sách người dùng cho thuộc tính liên kết")
    @RequiresPermission(anyOf = {
            QuyenHanMa.QUAN_LY_NGUOI_DUNG,
            QuyenHanMa.QUAN_LY_HOP_DONG,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG,
            QuyenHanMa.XEM_NHIEM_VU_NHA_THAU,
            // Vai trò chỉ có quyền Phân công (vd Trung tâm khu vực) vẫn phải nạp được danh sách
            // nhà thầu để chọn khi gán — trước đây thiếu mã này nên picker gán nhà thầu bị 403/rỗng.
            QuyenHanMa.QUAN_LY_PHAN_CONG,
    })
    public ResponseEntity<ApiResponse<List<NguoiDungThamChieuResponse>>> listThamChieu(
            @RequestParam String nhom,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<List<NguoiDungThamChieuResponse>>build()
                .withData(nguoiDungService.listThamChieu(nhom, search, activeOnly))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết người dùng")
    @RequiresPermission(anyOf = {
            QuyenHanMa.QUAN_LY_NGUOI_DUNG,
            QuyenHanMa.QUAN_LY_HOP_DONG,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG,
            QuyenHanMa.XEM_NHIEM_VU_NHA_THAU,
            QuyenHanMa.QUAN_LY_PHAN_CONG,
    })
    public ResponseEntity<ApiResponse<NguoiDungResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<NguoiDungResponse>build()
                .withData(nguoiDungService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo tài khoản (admin)")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> create(@Valid @RequestBody TaoTaiKhoanRequest request) {
        return ApiResponse.<NguoiDungResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(nguoiDungService.create(request))
                .withMessage("Đã tạo tài khoản")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật người dùng")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody NguoiDungCapNhatRequest request) {
        return ApiResponse.<NguoiDungResponse>build()
                .withData(nguoiDungService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm người dùng")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        nguoiDungService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }
}
