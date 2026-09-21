package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonLucDangTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.request.NguonViecLuuBangRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.services.NguonLucService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/nguon-luc")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.NguonLuc.TAG, description = ApiEntityCatalog.NguonLuc.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_NGUON_LUC)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class NguonLucController {

    private final NguonLucService nguonLucService;

    @GetMapping("/danh-sach")
    @Operation(summary = "Danh sách nguồn việc theo hợp đồng và overlay KV")
    public ResponseEntity<ApiResponse<NguonViecDanhSachResponse>> danhSach(
            @RequestParam(required = false) String trungTam,
            @RequestParam(required = false) String loaiCv,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String phapLy,
            @RequestParam(required = false) String nhanSu,
            @RequestParam(required = false) String linhVuc,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ApiResponse.<NguonViecDanhSachResponse>build()
                .withData(nguonLucService.danhSach(
                        trungTam, loaiCv, trangThai, phapLy, nhanSu, linhVuc, search, tab, tuNgay, denNgay))
                .toEntity();
    }

    @PutMapping("/luu-bang")
    @Operation(summary = "Lưu ghi chú KV, dòng thủ công và cột tuỳ chỉnh")
    public ResponseEntity<ApiResponse<NguonViecDanhSachResponse>> luuBang(
            @Valid @RequestBody NguonViecLuuBangRequest request) {
        return ApiResponse.<NguonViecDanhSachResponse>build()
                .withData(nguonLucService.luuBang(request))
                .toEntity();
    }
}
