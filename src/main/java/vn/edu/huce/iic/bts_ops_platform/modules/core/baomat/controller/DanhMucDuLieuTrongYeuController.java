package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.DanhMucDuLieuTrongYeuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.DanhMucDuLieuTrongYeuService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/danh-muc-du-lieu-trong-yeu")
@RequiredArgsConstructor
@Tag(name = "Danh mục dữ liệu trọng yếu", description = "Quản trị dữ liệu — phân loại mức bí mật và chủ sở hữu")
@RequiresPermission(QuyenHanMa.QUAN_LY_AUDIT_LOG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class DanhMucDuLieuTrongYeuController {

    private final DanhMucDuLieuTrongYeuService service;

    @GetMapping
    @Operation(summary = "Danh sách danh mục dữ liệu trọng yếu")
    public ResponseEntity<ApiResponse<List<DanhMucDuLieuTrongYeuResponse>>> list(
            @RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.<List<DanhMucDuLieuTrongYeuResponse>>build()
                .withData(service.list(activeOnly))
                .toEntity();
    }
}
