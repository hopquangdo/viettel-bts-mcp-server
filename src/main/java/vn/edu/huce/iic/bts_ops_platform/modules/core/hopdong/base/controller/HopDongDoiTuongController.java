package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongNhanBanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongXoaLoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongXoaTheoLocRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongXoaLoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongXoaTheoLocBatchResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.response.HopDongDoiTuongTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTonService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiDuyetService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong-doi-tuong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDongDoiTuong.TAG, description = ApiEntityCatalog.HopDongDoiTuong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongDoiTuongController {

    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongTonService hopDongDoiTuongTonService;
    private final HopDongDoiTuongTrangThaiDuyetService hopDongDoiTuongTrangThaiDuyetService;

    @GetMapping("/cho-quyet-toan")
    @Operation(summary = "Đối tượng đã HTTC, có pháp lý, chưa quyết toán, chưa quá hạn")
    public ResponseEntity<ApiResponse<PageResponse<HopDongDoiTuongTonItemResponse>>> choQuyetToan(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongDoiTuongTonItemResponse>>build()
                .withData(hopDongDoiTuongTonService.choQuyetToan(
                        loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, page, size))
                .toEntity();
    }

    @GetMapping("/qua-han")
    @Operation(summary = "Đối tượng đã HTTC, có pháp lý, chưa quyết toán, đã quá hạn")
    public ResponseEntity<ApiResponse<PageResponse<HopDongDoiTuongTonItemResponse>>> quaHan(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongDoiTuongTonItemResponse>>build()
                .withData(hopDongDoiTuongTonService.quaHan(
                        loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, page, size))
                .toEntity();
    }

    @GetMapping("/chua-phap-ly")
    @Operation(summary = "Đối tượng thuộc hợp đồng chưa có pháp lý")
    public ResponseEntity<ApiResponse<PageResponse<HopDongDoiTuongTonItemResponse>>> chuaPhapLy(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBaoCao,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongDoiTuongTonItemResponse>>build()
                .withData(hopDongDoiTuongTonService.chuaPhapLy(loaiHopDongId, ngayBaoCao, page, size))
                .toEntity();
    }

    @GetMapping
    @Operation(summary = "Danh sách HĐ — Đối tượng")
    public ResponseEntity<ApiResponse<PageResponse<HopDongDoiTuongResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @RequestParam(required = false) UUID trangThaiHopDongId,
            @RequestParam(required = false) Boolean withoutNhomUuTien,
            @RequestParam(required = false) Boolean withNhomUuTien,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongDoiTuongResponse>>build()
                .withData(hopDongDoiTuongService.list(
                        search,
                        activeOnly,
                        includeDeleted,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        page,
                        size))
                .toEntity();
    }

    @GetMapping("/ids")
    @Operation(summary = "Danh sách ID HĐ — Đối tượng (không enrich, dùng chọn tất cả)")
    public ResponseEntity<ApiResponse<PageResponse<UUID>>> listIds(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) UUID doiTuongQuanLyId,
            @RequestParam(required = false) UUID trangThaiHopDongId,
            @RequestParam(required = false) Boolean withoutNhomUuTien,
            @RequestParam(required = false) Boolean withNhomUuTien,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<UUID>>build()
                .withData(hopDongDoiTuongService.listIds(
                        search,
                        activeOnly,
                        includeDeleted,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        page,
                        size))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HopDongDoiTuongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HopDongDoiTuongResponse>build()
                .withData(hopDongDoiTuongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HopDongDoiTuongResponse>> create(@Valid @RequestBody HopDongDoiTuongTaoRequest request) {
        return ApiResponse.<HopDongDoiTuongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongDoiTuongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PostMapping("/nhan-ban")
    @Operation(summary = "Nhân bản đối tượng")
    public ResponseEntity<ApiResponse<List<HopDongDoiTuongResponse>>> nhanBan(
            @Valid @RequestBody HopDongDoiTuongNhanBanRequest request) {
        return ApiResponse.<List<HopDongDoiTuongResponse>>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongDoiTuongService.nhanBan(request.getMuc()))
                .withMessage("Đã nhân bản đối tượng")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HopDongDoiTuongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongDoiTuongCapNhatRequest request) {
        return ApiResponse.<HopDongDoiTuongResponse>build()
                .withData(hopDongDoiTuongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hopDongDoiTuongService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }

    @PostMapping("/xoa-lo")
    @Operation(summary = "Xóa mềm theo danh sách ID (bulk)")
    public ResponseEntity<ApiResponse<HopDongDoiTuongXoaLoResponse>> deleteBatch(
            @Valid @RequestBody HopDongDoiTuongXoaLoRequest request) {
        int deletedCount = hopDongDoiTuongService.deleteBatch(request.getIds());
        return ApiResponse.<HopDongDoiTuongXoaLoResponse>build()
                .withData(new HopDongDoiTuongXoaLoResponse(deletedCount))
                .withMessage("Đã xóa " + deletedCount + " bản ghi")
                .toEntity();
    }

    @PostMapping("/xoa-theo-loc")
    @Operation(summary = "Xóa mềm theo bộ lọc (một lần)")
    public ResponseEntity<ApiResponse<HopDongDoiTuongXoaLoResponse>> deleteByFilter(
            @Valid @RequestBody HopDongDoiTuongXoaTheoLocRequest request) {
        int deletedCount = hopDongDoiTuongService.deleteByFilter(request);
        return ApiResponse.<HopDongDoiTuongXoaLoResponse>build()
                .withData(new HopDongDoiTuongXoaLoResponse(deletedCount))
                .withMessage("Đã xóa " + deletedCount + " bản ghi")
                .toEntity();
    }

    @PostMapping("/trang-thai/yeu-cau-huy")
    @Operation(summary = "Gửi yêu cầu hủy đối tượng — chuyển sang Chờ xác nhận hủy")
    public ResponseEntity<ApiResponse<Integer>> yeuCauHuy(
            @Valid @RequestBody HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = hopDongDoiTuongTrangThaiDuyetService.yeuCauHuy(request);
        return ApiResponse.<Integer>build()
                .withData(updated)
                .withMessage("Đã gửi " + updated + " yêu cầu hủy chờ xác nhận")
                .toEntity();
    }

    @PostMapping("/trang-thai/yeu-cau-hoan-thanh")
    @Operation(summary = "Gửi yêu cầu hoàn thành — chuyển sang Chờ xác nhận hoàn thành")
    public ResponseEntity<ApiResponse<Integer>> yeuCauHoanThanh(
            @Valid @RequestBody HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = hopDongDoiTuongTrangThaiDuyetService.yeuCauHoanThanh(request);
        return ApiResponse.<Integer>build()
                .withData(updated)
                .withMessage("Đã gửi " + updated + " yêu cầu hoàn thành chờ xác nhận")
                .toEntity();
    }

    @PostMapping("/trang-thai/xac-nhan-huy")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    @Operation(summary = "Admin xác nhận hủy đối tượng (từ Chờ xác nhận hủy hoặc trực tiếp)")
    public ResponseEntity<ApiResponse<Integer>> xacNhanHuy(
            @Valid @RequestBody HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = hopDongDoiTuongTrangThaiDuyetService.xacNhanHuy(request);
        return ApiResponse.<Integer>build()
                .withData(updated)
                .withMessage("Đã xác nhận hủy " + updated + " đối tượng")
                .toEntity();
    }

    @PostMapping("/trang-thai/xac-nhan-hoan-thanh")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    @Operation(summary = "Admin xác nhận hoàn thành đối tượng (từ Chờ xác nhận HT hoặc trực tiếp)")
    public ResponseEntity<ApiResponse<Integer>> xacNhanHoanThanh(
            @Valid @RequestBody HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = hopDongDoiTuongTrangThaiDuyetService.xacNhanHoanThanh(request);
        return ApiResponse.<Integer>build()
                .withData(updated)
                .withMessage("Đã xác nhận hoàn thành " + updated + " đối tượng")
                .toEntity();
    }

    @PostMapping("/trang-thai/tu-choi-xac-nhan")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    @Operation(summary = "Từ chối yêu cầu chờ xác nhận — trả về trạng thái trước đó")
    public ResponseEntity<ApiResponse<Integer>> tuChoiXacNhan(
            @Valid @RequestBody HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = hopDongDoiTuongTrangThaiDuyetService.tuChoiXacNhan(request);
        return ApiResponse.<Integer>build()
                .withData(updated)
                .withMessage("Đã từ chối " + updated + " yêu cầu xác nhận")
                .toEntity();
    }

}
