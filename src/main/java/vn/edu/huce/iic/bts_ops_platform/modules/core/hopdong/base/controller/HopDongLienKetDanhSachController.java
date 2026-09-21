package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.services.HopDongLienKetService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hop-dong-lien-ket")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongLienKet.TAG, description = ApiEntityCatalog.HopDongLienKet.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongLienKetDanhSachController {

    private final HopDongLienKetService hopDongLienKetService;

    @GetMapping
    @Operation(summary = "Danh sách tất cả liên kết giữa các hợp đồng")
    @RequiresPermission(anyOf = {
            QuyenHanMa.QUAN_LY_HOP_DONG,
            QuyenHanMa.QUAN_LY_LOAI_HOP_DONG,
            QuyenHanMa.QUAN_LY_KIEU_HOP_DONG
    })
    public ResponseEntity<ApiResponse<List<HopDongLienKetResponse>>> listAll() {
        return ApiResponse.<List<HopDongLienKetResponse>>build()
                .withData(hopDongLienKetService.listAll())
                .toEntity();
    }
}
