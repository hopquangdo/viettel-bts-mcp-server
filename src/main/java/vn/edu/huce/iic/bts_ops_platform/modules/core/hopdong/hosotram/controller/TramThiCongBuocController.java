package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.request.TramThiCongBuocTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.TramThiCongBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.TramThiCongBuocService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong/{hopDongId}/ho-so-tram/{hopDongDoiTuongId}/thi-cong-buoc")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ trước thi công (tuần tự)")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
public class TramThiCongBuocController {

    private final TramThiCongBuocService tramThiCongBuocService;

    @GetMapping
    @Operation(summary = "Danh sách 4 bước hồ sơ trước thi công của trạm")
    public ResponseEntity<ApiResponse<List<TramThiCongBuocResponse>>> list(
            @PathVariable UUID hopDongId, @PathVariable UUID hopDongDoiTuongId) {
        return ApiResponse.<List<TramThiCongBuocResponse>>build()
                .withData(tramThiCongBuocService.list(hopDongId, hopDongDoiTuongId))
                .toEntity();
    }

    @PostMapping("/{maBuoc}/gui-duyet")
    @Operation(summary = "Gửi duyệt một bước hồ sơ trước thi công")
    public ResponseEntity<ApiResponse<TramThiCongBuocResponse>> guiDuyet(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @PathVariable String maBuoc) {
        return ApiResponse.<TramThiCongBuocResponse>build()
                .withData(tramThiCongBuocService.guiDuyet(hopDongId, hopDongDoiTuongId, maBuoc))
                .withMessage("Đã gửi duyệt")
                .toEntity();
    }

    @PostMapping("/{maBuoc}/duyet")
    @Operation(summary = "Duyệt một bước hồ sơ trước thi công")
    public ResponseEntity<ApiResponse<TramThiCongBuocResponse>> duyet(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @PathVariable String maBuoc) {
        return ApiResponse.<TramThiCongBuocResponse>build()
                .withData(tramThiCongBuocService.duyet(hopDongId, hopDongDoiTuongId, maBuoc))
                .withMessage("Đã duyệt bước hồ sơ")
                .toEntity();
    }

    @PostMapping("/{maBuoc}/tu-choi")
    @Operation(summary = "Từ chối một bước hồ sơ trước thi công")
    public ResponseEntity<ApiResponse<TramThiCongBuocResponse>> tuChoi(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @PathVariable String maBuoc,
            @Valid @RequestBody TramThiCongBuocTuChoiRequest request) {
        return ApiResponse.<TramThiCongBuocResponse>build()
                .withData(tramThiCongBuocService.tuChoi(
                        hopDongId, hopDongDoiTuongId, maBuoc, request.getLyDoTuChoi()))
                .withMessage("Đã từ chối bước hồ sơ")
                .toEntity();
    }
}
