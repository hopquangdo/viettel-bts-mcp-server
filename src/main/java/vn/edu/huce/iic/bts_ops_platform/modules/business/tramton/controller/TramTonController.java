package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.services.TramTonService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tram-ton")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.TramTon.TAG, description = ApiEntityCatalog.TramTon.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class TramTonController {

    private final TramTonService tramTonService;

    @GetMapping("/tong-quan")
    @Operation(summary = "Tổng quan trạm tồn (KPI + aging + lý do thiếu ĐK)")
    public ResponseEntity<ApiResponse<TramTonTongQuanResponse>> tongQuan(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String trungTam,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao) {
        return ApiResponse.<TramTonTongQuanResponse>build()
                .withData(tramTonService.tongQuan(
                        loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao))
                .toEntity();
    }

    @GetMapping("/danh-sach")
    @Operation(summary = "Danh sách trạm tồn phân trang theo tab/filter")
    public ResponseEntity<ApiResponse<PageResponse<TramTonItemResponse>>> danhSach(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String trungTam,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "all") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao) {
        return ApiResponse.<PageResponse<TramTonItemResponse>>build()
                .withData(tramTonService.danhSach(
                        loaiHopDongId, trungTam, dateFrom, dateTo, tab, search, page, size, quaHanNgay, ngayBaoCao))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{id}/chi-tiet")
    @Operation(summary = "Chi tiết đối tượng tồn — lý do và danh sách vướng mắc theo hạng mục/giai đoạn")
    public ResponseEntity<ApiResponse<TramTonChiTietResponse>> chiTiet(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao,
            @RequestParam(required = false, defaultValue = "false") boolean refresh) {
        return ApiResponse.<TramTonChiTietResponse>build()
                .withData(tramTonService.chiTiet(id, quaHanNgay, ngayBaoCao, refresh))
                .toEntity();
    }

}
