package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongImportBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuDownload;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeTatCaResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDong.TAG, description = ApiEntityCatalog.HopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongController {

    private final HopDongService hopDongService;

    @GetMapping
    @Operation(summary = "Danh sách Hợp đồng")
    public ResponseEntity<ApiResponse<PageResponse<HopDongResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID kieuHopDongId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongResponse>>build()
                .withData(hopDongService.list(
                        search, activeOnly, includeDeleted, loaiHopDongId, kieuHopDongId, page, size))
                .toEntity();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết")
    public ResponseEntity<ApiResponse<HopDongResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.<HopDongResponse>build()
                .withData(hopDongService.getById(id))
                .toEntity();
    }

    @PostMapping
    @Operation(summary = "Tạo mới")
    public ResponseEntity<ApiResponse<HopDongResponse>> create(@Valid @RequestBody HopDongTaoRequest request) {
        return ApiResponse.<HopDongResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongService.create(request))
                .withMessage("Đã tạo mới")
                .toEntity();
    }

    @PostMapping(value = "/{id}/tai-lieu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tài liệu hợp đồng")
    public ResponseEntity<ApiResponse<TepDinhKemResponse>> uploadTaiLieu(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String loaiTaiLieu) {
        return ApiResponse.<TepDinhKemResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(hopDongService.uploadTaiLieu(id, file, loaiTaiLieu))
                .withMessage("Đã upload tài liệu hợp đồng")
                .toEntity();
    }

    @GetMapping("/{id}/tai-lieu")
    @Operation(summary = "Danh sách tài liệu hợp đồng (hợp đồng + phụ lục)")
    public ResponseEntity<ApiResponse<List<HopDongTaiLieuItemResponse>>> listTaiLieu(@PathVariable UUID id) {
        return ApiResponse.<List<HopDongTaiLieuItemResponse>>build()
                .withData(hopDongService.listTaiLieu(id))
                .toEntity();
    }

    @DeleteMapping("/{id}/tai-lieu/{linkId}")
    @Operation(summary = "Xóa tài liệu hợp đồng")
    public ResponseEntity<ApiResponse<Void>> deleteTaiLieu(
            @PathVariable UUID id,
            @PathVariable UUID linkId) {
        hopDongService.deleteTaiLieu(id, linkId);
        return ApiResponse.<Void>build()
                .withMessage("Đã xóa tài liệu")
                .toEntity();
    }

    @GetMapping("/{id}/tai-lieu/{linkId}/tai-ve")
    @Operation(summary = "Tải tài liệu hợp đồng (hợp đồng / phụ lục)")
    public ResponseEntity<byte[]> downloadTaiLieu(@PathVariable UUID id, @PathVariable UUID linkId) {
        HopDongTaiLieuDownload file = hopDongService.downloadTaiLieu(id, linkId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }

    @GetMapping("/thong-ke")
    @Operation(summary = "Thống kê hợp đồng")
    public ResponseEntity<ApiResponse<Map<String, Object>>> thongKe(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) UUID kieuHopDongId) {
        return ApiResponse.<Map<String, Object>>build()
                .withData(hopDongService.thongKe(loaiHopDongId, kieuHopDongId))
                .toEntity();
    }

    @GetMapping("/thong-ke/tat-ca")
    @Operation(summary = "Thống kê hợp đồng của tất cả loại hợp đồng trong 1 lần gọi",
            description = "Cùng bộ số liệu như /thong-ke nhưng trả về cho mọi loại hợp đồng, "
                    + "với số query cố định — thay cho việc gọi /thong-ke lặp lại một lần mỗi loại.")
    public ResponseEntity<ApiResponse<HopDongThongKeTatCaResponse>> thongKeTatCa() {
        return ApiResponse.<HopDongThongKeTatCaResponse>build()
                .withData(hopDongService.thongKeTatCa())
                .toEntity();
    }

    @PostMapping(value = "/{id}/import-excel/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Xem trước import Excel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewImportExcel(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) UUID excelMappingId,
            @RequestParam(required = false) String tenSheet) {
        return ApiResponse.<Map<String, Object>>build()
                .withData(hopDongService.previewImportExcel(id, file, excelMappingId, tenSheet))
                .toEntity();
    }

    @PostMapping(value = "/{id}/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import Excel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importExcel(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) UUID excelMappingId,
            @RequestParam(required = false) String tenSheet,
            @RequestParam(required = false) Boolean importAsPending,
            @RequestParam(required = false) Boolean replacePending) {
        return ApiResponse.<Map<String, Object>>build()
                .withData(hopDongService.importExcel(id, file, excelMappingId, tenSheet, importAsPending, replacePending))
                .withMessage("Đã nhận file import")
                .toEntity();
    }

    @PostMapping("/{id}/import-excel/doi-tuong-batch")
    @Operation(summary = "Import đối tượng theo lô (JSON)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importDoiTuongBatch(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongDoiTuongImportBatchRequest request) {
        return ApiResponse.<Map<String, Object>>build()
                .withData(hopDongService.importDoiTuongBatch(id, request))
                .withMessage("Đã xử lý lô import")
                .toEntity();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật")
    public ResponseEntity<ApiResponse<HopDongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HopDongCapNhatRequest request) {
        return ApiResponse.<HopDongResponse>build()
                .withData(hopDongService.update(id, request))
                .withMessage("Đã cập nhật")
                .toEntity();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hopDongService.delete(id);
        return ApiResponse.<Void>build().withMessage("Đã xóa").toEntity();
    }

    @PostMapping("/{id}/huy")
    @Operation(summary = "Hủy hợp đồng — vô hiệu hóa HĐ và toàn bộ đối tượng con, vẫn xem được chi tiết")
    public ResponseEntity<ApiResponse<HopDongResponse>> cancel(@PathVariable UUID id) {
        return ApiResponse.<HopDongResponse>build()
                .withData(hopDongService.cancel(id))
                .withMessage("Đã hủy hợp đồng")
                .toEntity();
    }
}
