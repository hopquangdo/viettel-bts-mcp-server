package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatDoiTuongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongNghiemThuRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoHangLoatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.*;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/san-luong-thi-cong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.SanLuong.TAG, description = "Sản lượng thi công theo đối tượng hợp đồng")
@RequiresPermission(QuyenHanMa.QUAN_LY_SAN_LUONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class SanLuongController {

    private final SanLuongService sanLuongService;

    @GetMapping("/doi-tuong")
    @Operation(summary = "Danh sách đối tượng hợp đồng và tiến độ sản lượng (phân trang page/size)")
    public ResponseEntity<ApiResponse<PageResponse<SanLuongDoiTuongRowResponse>>> listDoiTuong(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @RequestParam(required = false) List<UUID> doiTuongQuanLyIds,
            @RequestParam(required = false) UUID contractorId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "false") boolean includeWithoutOutput,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<SanLuongDoiTuongRowResponse>>build()
                .withData(sanLuongService.listDoiTuong(
                        hopDongId,
                        mergeDoiTuongQuanLyFilter(doiTuongQuanLyId, doiTuongQuanLyIds),
                        contractorId,
                        dateFrom,
                        dateTo,
                        includeWithoutOutput,
                        page,
                        size))
                .toEntity();
    }

    @GetMapping("/doi-tuong/tim-kiem")
    @Operation(summary = "Tìm kiếm đối tượng theo mã/tên/giá trị thuộc tính (bỏ lọc theo ngày)")
    public ResponseEntity<ApiResponse<PageResponse<SanLuongDoiTuongRowResponse>>> searchDoiTuong(
            @RequestParam String search,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @RequestParam(required = false) List<UUID> doiTuongQuanLyIds,
            @RequestParam(required = false) UUID contractorId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<SanLuongDoiTuongRowResponse>>build()
                .withData(sanLuongService.searchDoiTuong(
                        search,
                        hopDongId,
                        mergeDoiTuongQuanLyFilter(doiTuongQuanLyId, doiTuongQuanLyIds),
                        contractorId,
                        page,
                        size))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tong-khao-sat-km")
    @Operation(summary = "Tổng km khảo sát của hợp đồng (trangThai=survey)")
    public ResponseEntity<ApiResponse<BigDecimal>> tongKhaoSatKmTheoHopDong(@PathVariable UUID hopDongId) {
        return ApiResponse.<BigDecimal>build()
                .withData(sanLuongService.tongKhaoSatKmTheoHopDong(hopDongId))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tong-thanh-tien")
    @Operation(summary = "Tổng thành tiền sản lượng của hợp đồng")
    public ResponseEntity<ApiResponse<BigDecimal>> tongThanhTienTheoHopDong(@PathVariable UUID hopDongId) {
        return ApiResponse.<BigDecimal>build()
                .withData(sanLuongService.tongThanhTienTheoHopDong(hopDongId))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{hopDongDoiTuongId}/tong-thanh-tien")
    @Operation(summary = "Tổng thành tiền sản lượng của đối tượng hợp đồng")
    public ResponseEntity<ApiResponse<BigDecimal>> tongThanhTienTheoDoiTuong(
            @PathVariable UUID hopDongDoiTuongId) {
        BigDecimal total = sanLuongService.tongThanhTienTheoDoiTuongIds(List.of(hopDongDoiTuongId))
                .getOrDefault(hopDongDoiTuongId, BigDecimal.ZERO);
        return ApiResponse.<BigDecimal>build()
                .withData(total)
                .toEntity();
    }

    @GetMapping("/doi-tuong/{hopDongDoiTuongId}")
    @Operation(summary = "Chi tiết sản lượng theo đối tượng hợp đồng")
    public ResponseEntity<ApiResponse<SanLuongDoiTuongChiTietResponse>> getDoiTuongChiTiet(
            @PathVariable UUID hopDongDoiTuongId,
            @RequestParam(required = false, defaultValue = "false") boolean includeWorkItems) {
        return ApiResponse.<SanLuongDoiTuongChiTietResponse>build()
                .withData(sanLuongService.getDoiTuongChiTiet(hopDongDoiTuongId, includeWorkItems))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{hopDongDoiTuongId}/hang-muc")
    @Operation(summary = "Danh sách hạng mục sản lượng theo trang")
    public ResponseEntity<ApiResponse<PageResponse<SanLuongHangMucItemResponse>>> listHangMuc(
            @PathVariable UUID hopDongDoiTuongId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<SanLuongHangMucItemResponse>>build()
                .withData(sanLuongService.listHangMuc(hopDongDoiTuongId, page, size))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{hopDongDoiTuongId}/nhat-ky")
    @Operation(summary = "Nhật ký bổ sung sản lượng theo đối tượng hợp đồng")
    public ResponseEntity<ApiResponse<SanLuongNhatKyResponse>> listNhatKy(
            @PathVariable UUID hopDongDoiTuongId) {
        return ApiResponse.<SanLuongNhatKyResponse>build()
                .withData(sanLuongService.listNhatKy(hopDongDoiTuongId))
                .toEntity();
    }

    @GetMapping("/tong-hop")
    @Operation(summary = "Thống kê sản lượng")
    public ResponseEntity<ApiResponse<SanLuongTongHopResponse>> tongHop(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @RequestParam(required = false) List<UUID> doiTuongQuanLyIds,
            @RequestParam(required = false) UUID contractorId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        return ApiResponse.<SanLuongTongHopResponse>build()
                .withData(sanLuongService.tongHop(
                        search,
                        hopDongId,
                        mergeDoiTuongQuanLyFilter(doiTuongQuanLyId, doiTuongQuanLyIds),
                        contractorId,
                        dateFrom,
                        dateTo))
                .toEntity();
    }

    @GetMapping("/bo-loc")
    @Operation(summary = "Tùy chọn bộ lọc")
    public ResponseEntity<ApiResponse<SanLuongBoLocResponse>> boLoc() {
        return ApiResponse.<SanLuongBoLocResponse>build()
                .withData(sanLuongService.boLoc())
                .toEntity();
    }

    @PostMapping("/tao-hang-loat")
    @Operation(summary = "Ghi nhận sản lượng theo hạng mục công việc")
    public ResponseEntity<ApiResponse<SanLuongDoiTuongRowResponse>> taoHangLoat(
            @Valid @RequestBody SanLuongTaoHangLoatRequest request) {
        return ApiResponse.<SanLuongDoiTuongRowResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(sanLuongService.taoHangLoat(request))
                .withMessage("Đã tạo sản lượng")
                .toEntity();
    }

    @PutMapping("/{id}/nghiem-thu")
    @RequiresPermission(QuyenHanMa.NGHIEM_THU_SAN_LUONG)
    @Operation(summary = "Ghi nhận kết quả nghiệm thu (đạt/không đạt) cho 1 bản ghi sản lượng — bắt buộc lý do khi không đạt")
    public ResponseEntity<ApiResponse<SanLuongResponse>> nghiemThu(
            @PathVariable UUID id,
            @RequestBody SanLuongNghiemThuRequest request) {
        return ApiResponse.<SanLuongResponse>build()
                .withData(sanLuongService.nghiemThu(id, request.getKetQua(), request.getLyDo()))
                .withMessage("Đã ghi nhận kết quả nghiệm thu")
                .toEntity();
    }

    @PutMapping("/doi-tuong/{hopDongDoiTuongId}")
    @Operation(summary = "Cập nhật sản lượng đối tượng hợp đồng")
    public ResponseEntity<ApiResponse<SanLuongDoiTuongRowResponse>> capNhatDoiTuong(
            @PathVariable UUID hopDongDoiTuongId,
            @Valid @RequestBody SanLuongCapNhatDoiTuongRequest request) {
        return ApiResponse.<SanLuongDoiTuongRowResponse>build()
                .withData(sanLuongService.capNhatDoiTuong(hopDongDoiTuongId, request))
                .withMessage("Đã cập nhật sản lượng")
                .toEntity();
    }

    @PostMapping("/doi-tuong/{hopDongDoiTuongId}/xac-nhan-hoan-thanh")
    @Operation(summary = "Xác nhận đối tượng đã hoàn thành thi công (100% tiến độ, không đổi hạng mục chưa làm)")
    public ResponseEntity<ApiResponse<SanLuongDoiTuongRowResponse>> xacNhanHoanThanhDoiTuong(
            @PathVariable UUID hopDongDoiTuongId) {
        return ApiResponse.<SanLuongDoiTuongRowResponse>build()
                .withData(sanLuongService.xacNhanHoanThanhDoiTuong(hopDongDoiTuongId))
                .withMessage("Đã xác nhận hoàn thành")
                .toEntity();
    }

    @PostMapping(value = "/doi-tuong/{hopDongDoiTuongId}/anh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải ảnh sản lượng thi công")
    public ResponseEntity<ApiResponse<SanLuongAnhResponse>> uploadAnh(
            @PathVariable UUID hopDongDoiTuongId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID hangMucCongViecId,
            @RequestParam(required = false) UUID hangMucChiTietId,
            @RequestParam(required = false, defaultValue = "construction") String loaiAnh,
            @RequestParam(required = false) String moTa) {
        return ApiResponse.<SanLuongAnhResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(sanLuongService.uploadAnh(
                        hopDongDoiTuongId, file, hangMucCongViecId, hangMucChiTietId, loaiAnh, moTa))
                .withMessage("Đã tải ảnh")
                .toEntity();
    }

    @DeleteMapping("/doi-tuong/{hopDongDoiTuongId}")
    @Operation(summary = "Xóa đối tượng khỏi danh sách sản lượng thi công")
    public ResponseEntity<ApiResponse<Void>> xoaDoiTuong(@PathVariable UUID hopDongDoiTuongId) {
        sanLuongService.xoaDoiTuong(hopDongDoiTuongId);
        return ApiResponse.<Void>build().withMessage("Đã xóa đối tượng khỏi danh sách sản lượng").toEntity();
    }

    private static List<UUID> mergeDoiTuongQuanLyFilter(UUID singleId, List<UUID> multipleIds) {
        if (singleId != null) {
            return List.of(singleId);
        }
        if (multipleIds == null || multipleIds.isEmpty()) {
            return List.of();
        }
        return multipleIds.stream().filter(Objects::nonNull).distinct().toList();
    }
}
