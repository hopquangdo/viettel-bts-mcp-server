package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.PhanQuyenQuyenHanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.PhanQuyenQuyenHanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenQuyenHanService;

import java.util.List;

/** Ma trận phân quyền (vai trò × quyền hạn). Chỉ SUPERADMIN hoặc vai trò được cấp
 * QUAN_LY_PHAN_QUYEN mới xem/sửa được ma trận; riêng /cua-toi mở cho mọi người đã đăng nhập để
 * frontend biết ẩn/hiện menu, nút bấm. */
@RestController
@RequestMapping("/api/v1/phan-quyen-quyen-han")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.PhanQuyenQuyenHan.TAG, description = ApiEntityCatalog.PhanQuyenQuyenHan.DESCRIPTION)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class PhanQuyenQuyenHanController {

    private final PhanQuyenQuyenHanService phanQuyenQuyenHanService;

    @GetMapping
    @Operation(summary = "Các quyền hạn đang bật của 1 vai trò trong ma trận")
    @RequiresPermission(QuyenHanMa.QUAN_LY_PHAN_QUYEN)
    public ResponseEntity<ApiResponse<List<PhanQuyenQuyenHanResponse>>> list(@RequestParam String quyenMa) {
        return ApiResponse.<List<PhanQuyenQuyenHanResponse>>build()
                .withData(phanQuyenQuyenHanService.list(quyenMa))
                .toEntity();
    }

    @PutMapping("/dong-bo")
    @Operation(summary = "Đồng bộ (thay cả bộ) quyền hạn cho 1 vai trò — cache authority tự làm mới")
    @RequiresPermission(QuyenHanMa.QUAN_LY_PHAN_QUYEN)
    public ResponseEntity<ApiResponse<List<PhanQuyenQuyenHanResponse>>> sync(
            @Valid @RequestBody PhanQuyenQuyenHanDongBoRequest request) {
        return ApiResponse.<List<PhanQuyenQuyenHanResponse>>build()
                .withData(phanQuyenQuyenHanService.sync(request))
                .withMessage("Đã cập nhật ma trận phân quyền")
                .toEntity();
    }

    @GetMapping("/cua-toi")
    @Operation(summary = "Mã quyền hạn của chính người đang đăng nhập — không cần quyền quản trị")
    public ResponseEntity<ApiResponse<List<String>>> cuaToi() {
        return ApiResponse.<List<String>>build()
                .withData(phanQuyenQuyenHanService.cuaToi())
                .toEntity();
    }
}
