package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongLienKetCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.services.HopDongLienKetService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong/{hopDongId}/lien-ket")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongLienKet.TAG, description = ApiEntityCatalog.HopDongLienKet.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongLienKetController {

    private final HopDongLienKetService hopDongLienKetService;

    @GetMapping
    @Operation(summary = "Liên kết của một hợp đồng")
    public ResponseEntity<ApiResponse<List<HopDongLienKetResponse>>> list(@PathVariable UUID hopDongId) {
        return ApiResponse.<List<HopDongLienKetResponse>>build()
                .withData(hopDongLienKetService.listByHopDongId(hopDongId))
                .toEntity();
    }

    @PutMapping
    @Operation(summary = "Cập nhật HĐ liên kết của một hợp đồng")
    public ResponseEntity<ApiResponse<List<HopDongLienKetResponse>>> sync(
            @PathVariable UUID hopDongId,
            @Valid @RequestBody HopDongLienKetCapNhatRequest request) {
        return ApiResponse.<List<HopDongLienKetResponse>>build()
                .withData(hopDongLienKetService.sync(hopDongId, request))
                .withMessage("Đã cập nhật liên kết hợp đồng")
                .toEntity();
    }
}
