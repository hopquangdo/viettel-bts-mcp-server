package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.request.PhanAnhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.PhanAnhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.PhanAnhService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/phan-anh")
@RequiredArgsConstructor
@Tag(name = "Phản ánh người dùng", description = "Người dùng tự gửi phản ánh lỗi / vướng mắc hệ thống")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class PhanAnhController {

    private final PhanAnhService phanAnhService;

    @PostMapping
    @Operation(summary = "Gửi phản ánh mới")
    public ResponseEntity<ApiResponse<PhanAnhResponse>> create(@Valid @RequestBody PhanAnhTaoRequest request) {
        return ApiResponse.<PhanAnhResponse>build()
                .withData(phanAnhService.create(request))
                .withMessage("Đã gửi phản ánh")
                .toEntity();
    }

    @GetMapping("/cua-toi")
    @Operation(summary = "Danh sách phản ánh của tôi")
    public ResponseEntity<ApiResponse<List<PhanAnhResponse>>> listMine() {
        return ApiResponse.<List<PhanAnhResponse>>build()
                .withData(phanAnhService.listMine())
                .toEntity();
    }

    @GetMapping
    @RequiresPermission(QuyenHanMa.QUAN_LY_AUDIT_LOG)
    @Operation(summary = "Danh sách tất cả phản ánh (quản trị)")
    public ResponseEntity<ApiResponse<List<PhanAnhResponse>>> listAll() {
        return ApiResponse.<List<PhanAnhResponse>>build()
                .withData(phanAnhService.listAll())
                .toEntity();
    }

    @PatchMapping("/{id}/trang-thai")
    @RequiresPermission(QuyenHanMa.QUAN_LY_AUDIT_LOG)
    @Operation(summary = "Cập nhật trạng thái phản ánh")
    public ResponseEntity<ApiResponse<PhanAnhResponse>> updateTrangThai(
            @PathVariable UUID id,
            @RequestParam String trangThai) {
        return ApiResponse.<PhanAnhResponse>build()
                .withData(phanAnhService.updateTrangThai(id, trangThai))
                .withMessage("Đã cập nhật trạng thái")
                .toEntity();
    }
}
