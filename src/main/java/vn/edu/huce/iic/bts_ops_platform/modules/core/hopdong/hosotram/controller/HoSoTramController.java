package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.HoSoTramTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.HoSoTramService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong/{hopDongId}/ho-so-tram/{hopDongDoiTuongId}")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ trạm (tải lên thủ công)")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
public class HoSoTramController {

    private final HoSoTramService hoSoTramService;

    @GetMapping
    @Operation(summary = "Danh sách file hồ sơ đã tải lên thủ công của 1 trạm")
    public ResponseEntity<ApiResponse<List<HoSoTramTepDinhKemResponse>>> list(
            @PathVariable UUID hopDongId, @PathVariable UUID hopDongDoiTuongId) {
        return ApiResponse.<List<HoSoTramTepDinhKemResponse>>build()
                .withData(hoSoTramService.list(hopDongId, hopDongDoiTuongId))
                .toEntity();
    }

    @PostMapping(value = "/tep-dinh-kem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên 1 file hồ sơ cho 1 danh mục của trạm (Bàn giao mặt bằng, "
            + "Giao nhiệm vụ GS-TC, Nghiệm thu đầu vào vật liệu, Hồ sơ thiết kế, Thi công)")
    public ResponseEntity<ApiResponse<HoSoTramTepDinhKemResponse>> upload(
            @PathVariable UUID hopDongId,
            @PathVariable UUID hopDongDoiTuongId,
            @RequestParam String danhMuc,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String ghiChu) {
        return ApiResponse.<HoSoTramTepDinhKemResponse>build()
                .withData(hoSoTramService.upload(hopDongId, hopDongDoiTuongId, danhMuc, file, ghiChu))
                .withMessage("Đã tải lên file")
                .toEntity();
    }

    @DeleteMapping("/tep-dinh-kem/{id}")
    @Operation(summary = "Gỡ 1 file đã tải nhầm")
    public ResponseEntity<ApiResponse<Void>> xoa(
            @PathVariable UUID hopDongId, @PathVariable UUID hopDongDoiTuongId, @PathVariable UUID id) {
        hoSoTramService.xoa(hopDongId, hopDongDoiTuongId, id);
        return ApiResponse.<Void>build().withMessage("Đã gỡ file").toEntity();
    }
}
