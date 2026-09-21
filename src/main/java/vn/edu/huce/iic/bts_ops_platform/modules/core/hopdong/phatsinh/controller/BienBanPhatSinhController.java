package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request.BienBanPhatSinhKyPhuLucRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request.BienBanPhatSinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request.BienBanPhatSinhTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response.BienBanPhatSinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.service.BienBanPhatSinhService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong/{hopDongId}/bien-ban-phat-sinh")
@RequiredArgsConstructor
@Tag(name = "Biên bản phát sinh khối lượng")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
public class BienBanPhatSinhController {

    private final BienBanPhatSinhService bienBanPhatSinhService;

    @GetMapping
    @Operation(summary = "Danh sách biên bản phát sinh khối lượng của 1 hợp đồng")
    public ResponseEntity<ApiResponse<List<BienBanPhatSinhResponse>>> list(@PathVariable UUID hopDongId) {
        return ApiResponse.<List<BienBanPhatSinhResponse>>build()
                .withData(bienBanPhatSinhService.list(hopDongId))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Lập biên bản phát sinh khối lượng mới — trạng thái ban đầu Chờ duyệt")
    public ResponseEntity<ApiResponse<BienBanPhatSinhResponse>> create(
            @PathVariable UUID hopDongId,
            @RequestBody BienBanPhatSinhTaoRequest request) {
        return ApiResponse.<BienBanPhatSinhResponse>build()
                .withData(bienBanPhatSinhService.create(hopDongId, request))
                .withMessage("Đã lập biên bản phát sinh")
                .toEntity();
    }

    @PostMapping(value = "/{id}/tep-dinh-kem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Đính kèm file thiết kế/dự toán điều chỉnh vào biên bản phát sinh")
    public ResponseEntity<ApiResponse<BienBanPhatSinhResponse>> uploadTepDinhKem(
            @PathVariable UUID hopDongId,
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "KHAC") String loaiTaiLieu,
            @RequestParam(required = false) String ghiChu) {
        return ApiResponse.<BienBanPhatSinhResponse>build()
                .withData(bienBanPhatSinhService.uploadTepDinhKem(hopDongId, id, file, loaiTaiLieu, ghiChu))
                .withMessage("Đã đính kèm file")
                .toEntity();
    }

    @PutMapping("/{id}/phe-duyet")
    @Operation(summary = "Duyệt biên bản phát sinh đang ở trạng thái chờ duyệt")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<BienBanPhatSinhResponse>> pheDuyet(
            @PathVariable UUID hopDongId, @PathVariable UUID id) {
        return ApiResponse.<BienBanPhatSinhResponse>build()
                .withData(bienBanPhatSinhService.pheDuyet(hopDongId, id))
                .toEntity();
    }

    @PutMapping("/{id}/tu-choi")
    @Operation(summary = "Từ chối biên bản phát sinh đang ở trạng thái chờ duyệt")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<BienBanPhatSinhResponse>> tuChoi(
            @PathVariable UUID hopDongId,
            @PathVariable UUID id,
            @RequestBody BienBanPhatSinhTuChoiRequest request) {
        return ApiResponse.<BienBanPhatSinhResponse>build()
                .withData(bienBanPhatSinhService.tuChoi(hopDongId, id, request.getLyDo()))
                .toEntity();
    }

    @PutMapping("/{id}/ky-phu-luc")
    @Operation(summary = "Ghi ngày ký phụ lục HĐ cho phần phát sinh đã duyệt (ký thật ngoài hệ thống)")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<BienBanPhatSinhResponse>> kyPhuLuc(
            @PathVariable UUID hopDongId,
            @PathVariable UUID id,
            @RequestBody(required = false) BienBanPhatSinhKyPhuLucRequest request) {
        return ApiResponse.<BienBanPhatSinhResponse>build()
                .withData(bienBanPhatSinhService.kyPhuLuc(hopDongId, id, request != null ? request.getNgayKy() : null))
                .withMessage("Đã ghi ngày ký phụ lục")
                .toEntity();
    }
}
