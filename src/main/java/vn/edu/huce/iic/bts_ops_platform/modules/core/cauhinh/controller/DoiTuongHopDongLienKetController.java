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
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongHopDongLienKetDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongHopDongLienKetService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doi-tuong-hop-dong-lien-ket")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.DoiTuongHopDongLienKet.TAG, description = ApiEntityCatalog.DoiTuongHopDongLienKet.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_DOI_TUONG_QUAN_LY)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class DoiTuongHopDongLienKetController {

    private final DoiTuongHopDongLienKetService doiTuongHopDongLienKetService;

    @GetMapping
    @Operation(summary = "Danh sách liên kết đối tượng — loại/kiểu HĐ")
    @RequiresPermission(anyOf = {
            QuyenHanMa.QUAN_LY_DOI_TUONG_QUAN_LY,
            QuyenHanMa.QUAN_LY_HOP_DONG,
            QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG
    })
    public ResponseEntity<ApiResponse<List<DoiTuongHopDongLienKetResponse>>> list(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID kieuHopDongId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false"
            ) boolean includeDeleted) {
        return ApiResponse.<List<DoiTuongHopDongLienKetResponse>>build()
                .withData(doiTuongHopDongLienKetService.list(loaiHopDongId, kieuHopDongId, activeOnly, includeDeleted))
                .toEntity();
    }

    @PutMapping("/dong-bo")
    @Operation(summary = "Đồng bộ danh sách đối tượng gắn với loại hoặc kiểu HĐ")
    public ResponseEntity<ApiResponse<List<DoiTuongHopDongLienKetResponse>>> sync(
            @Valid @RequestBody DoiTuongHopDongLienKetDongBoRequest request) {
        return ApiResponse.<List<DoiTuongHopDongLienKetResponse>>build()
                .withData(doiTuongHopDongLienKetService.sync(request))
                .withMessage("Đã cập nhật liên kết đối tượng")
                .toEntity();
    }

}
