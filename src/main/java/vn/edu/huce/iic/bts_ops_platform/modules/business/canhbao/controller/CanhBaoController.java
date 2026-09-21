package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.CanhBaoNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.SanLuongDaKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response.TyLeHuyTramResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.services.CanhBaoService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/canh-bao")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.CanhBao.TAG, description = ApiEntityCatalog.CanhBao.DESCRIPTION)
@RequiresPermission(anyOf = {
        QuyenHanMa.QUAN_LY_HOP_DONG,
        QuyenHanMa.QUAN_LY_SAN_LUONG
})
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class CanhBaoController {

    private final CanhBaoService canhBaoService;

    @GetMapping("/ty-le-huy-tram")
    @Operation(summary = "Thống kê tỷ lệ hủy trạm theo cán bộ và khu vực")
    public ResponseEntity<ApiResponse<TyLeHuyTramResponse>> tyLeHuyTram() {
        return ApiResponse.<TyLeHuyTramResponse>build()
                .withData(canhBaoService.tyLeHuyTram())
                .toEntity();
    }

    @GetMapping("/nguong/ty-le-huy-tram")
    @Operation(summary = "Lấy ngưỡng cảnh báo tỷ lệ hủy trạm (%)")
    public ResponseEntity<ApiResponse<BigDecimal>> nguongTyLeHuy() {
        return ApiResponse.<BigDecimal>build()
                .withData(canhBaoService.getNguongTyLeHuy())
                .toEntity();
    }

    @PutMapping("/nguong/ty-le-huy-tram")
    @Operation(summary = "Cập nhật ngưỡng cảnh báo tỷ lệ hủy trạm (%)")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
    public ResponseEntity<ApiResponse<BigDecimal>> capNhatNguongTyLeHuy(
            @Valid @RequestBody CanhBaoNguongCapNhatRequest request) {
        return ApiResponse.<BigDecimal>build()
                .withData(canhBaoService.capNhatNguongTyLeHuy(request))
                .withMessage("Đã cập nhật ngưỡng")
                .toEntity();
    }

    @PatchMapping("/san-luong-bat-thuong/{hopDongDoiTuongId}/da-kiem-tra")
    @Operation(summary = "Đánh dấu trạm sản lượng bất thường đã kiểm tra")
    public ResponseEntity<ApiResponse<Void>> danhDauDaKiemTra(
            @PathVariable UUID hopDongDoiTuongId,
            @RequestBody(required = false) SanLuongDaKiemTraRequest request) {
        canhBaoService.danhDauSanLuongDaKiemTra(hopDongDoiTuongId, request);
        return ApiResponse.<Void>build().withMessage("Đã ghi nhận kiểm tra").toEntity();
    }
}
