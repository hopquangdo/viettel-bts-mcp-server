package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.ChecklistDamBaoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongChecklistDapUngCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongTienDoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.HopDongChecklistSyncRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachImportChiTietRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTrienKhaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KpiNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuYeuCauTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.ChecklistDamBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongChecklistDapUngResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.HopDongChecklistResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KeHoachTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiNguongCauHinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.VatTuYeuCauResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.service.TienDoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tien-do")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.TienDo.TAG, description = ApiEntityCatalog.TienDo.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class TienDoController {

    private final TienDoService tienDoService;

    @GetMapping("/kpi-canh-bao")
    @Operation(summary = "Danh sách cảnh báo KPI tiến độ")
    public ResponseEntity<ApiResponse<List<KpiCanhBaoResponse>>> listKpiCanhBao() {
        return ApiResponse.<List<KpiCanhBaoResponse>>build()
                .withData(tienDoService.listKpiCanhBao())
                .toEntity();
    }

    @GetMapping("/doi-tuong/{id}")
    @Operation(summary = "Chi tiết tiến độ đối tượng HĐ")
    public ResponseEntity<ApiResponse<DoiTuongTienDoResponse>> getDoiTuongTienDo(@PathVariable UUID id) {
        return ApiResponse.<DoiTuongTienDoResponse>build()
                .withData(tienDoService.getDoiTuongTienDo(id))
                .toEntity();
    }

    @PutMapping("/doi-tuong/{id}")
    @Operation(summary = "Cập nhật tiến độ đối tượng HĐ")
    public ResponseEntity<ApiResponse<DoiTuongTienDoResponse>> capNhatDoiTuongTienDo(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTuongTienDoCapNhatRequest request) {
        return ApiResponse.<DoiTuongTienDoResponse>build()
                .withData(tienDoService.capNhatDoiTuongTienDo(id, request))
                .withMessage("Đã cập nhật tiến độ đối tượng")
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/checklist-dam-bao")
    @Operation(summary = "Checklist điều kiện đảm bảo triển khai")
    public ResponseEntity<ApiResponse<ChecklistDamBaoResponse>> getChecklistDamBao(
            @PathVariable UUID hopDongId) {
        return ApiResponse.<ChecklistDamBaoResponse>build()
                .withData(tienDoService.getChecklistDamBao(hopDongId))
                .toEntity();
    }

    @PutMapping("/hop-dong/{hopDongId}/checklist-dam-bao")
    @Operation(summary = "Cập nhật checklist điều kiện đảm bảo triển khai (legacy)")
    public ResponseEntity<ApiResponse<ChecklistDamBaoResponse>> capNhatChecklistDamBao(
            @PathVariable UUID hopDongId,
            @Valid @RequestBody ChecklistDamBaoCapNhatRequest request) {
        return ApiResponse.<ChecklistDamBaoResponse>build()
                .withData(tienDoService.capNhatChecklistDamBao(hopDongId, request))
                .withMessage("Đã cập nhật checklist")
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/checklist")
    @Operation(summary = "Danh sách mục checklist theo hợp đồng")
    public ResponseEntity<ApiResponse<HopDongChecklistResponse>> getHopDongChecklist(
            @PathVariable UUID hopDongId) {
        return ApiResponse.<HopDongChecklistResponse>build()
                .withData(tienDoService.getHopDongChecklist(hopDongId))
                .toEntity();
    }

    @PutMapping("/hop-dong/{hopDongId}/checklist")
    @Operation(summary = "Đồng bộ danh sách mục checklist của hợp đồng")
    public ResponseEntity<ApiResponse<HopDongChecklistResponse>> syncHopDongChecklist(
            @PathVariable UUID hopDongId,
            @Valid @RequestBody HopDongChecklistSyncRequest request) {
        return ApiResponse.<HopDongChecklistResponse>build()
                .withData(tienDoService.syncHopDongChecklist(hopDongId, request))
                .withMessage("Đã cập nhật danh sách checklist")
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/checklist-dap-ung")
    @Operation(summary = "Trạng thái duyệt checklist theo đối tượng")
    public ResponseEntity<ApiResponse<List<DoiTuongChecklistDapUngResponse>>> listDoiTuongChecklistDapUng(
            @PathVariable UUID hopDongId,
            @RequestParam List<UUID> doiTuongIds) {
        return ApiResponse.<List<DoiTuongChecklistDapUngResponse>>build()
                .withData(tienDoService.listDoiTuongChecklistDapUng(hopDongId, doiTuongIds))
                .toEntity();
    }

    @PutMapping("/doi-tuong/{id}/checklist-dap-ung")
    @Operation(summary = "Duyệt / bỏ duyệt 1 mục checklist của đối tượng")
    public ResponseEntity<ApiResponse<DoiTuongChecklistDapUngResponse>> capNhatDoiTuongChecklistDapUng(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTuongChecklistDapUngCapNhatRequest request) {
        return ApiResponse.<DoiTuongChecklistDapUngResponse>build()
                .withData(tienDoService.capNhatDoiTuongChecklistDapUng(id, request))
                .withMessage("Đã cập nhật trạng thái checklist")
                .toEntity();
    }

    @GetMapping("/ke-hoach")
    @Operation(summary = "Danh sách kế hoạch triển khai")
    public ResponseEntity<ApiResponse<List<KeHoachTrienKhaiResponse>>> listKeHoach(
            @RequestParam(required = false) UUID hopDongId) {
        return ApiResponse.<List<KeHoachTrienKhaiResponse>>build()
                .withData(tienDoService.listKeHoach(hopDongId))
                .toEntity();
    }

    @PostMapping("/ke-hoach")
    @Operation(summary = "Tạo kế hoạch triển khai")
    public ResponseEntity<ApiResponse<KeHoachTrienKhaiResponse>> taoKeHoach(
            @Valid @RequestBody KeHoachTrienKhaiTaoRequest request) {
        return ApiResponse.<KeHoachTrienKhaiResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(tienDoService.taoKeHoach(request))
                .withMessage("Đã tạo kế hoạch triển khai")
                .toEntity();
    }

    @PostMapping("/ke-hoach/{id}/gui-duyet")
    @Operation(summary = "Gửi kế hoạch triển khai duyệt")
    public ResponseEntity<ApiResponse<KeHoachTrienKhaiResponse>> guiKeHoachDuyet(@PathVariable UUID id) {
        return ApiResponse.<KeHoachTrienKhaiResponse>build()
                .withData(tienDoService.guiKeHoachDuyet(id))
                .withMessage("Đã gửi kế hoạch duyệt")
                .toEntity();
    }

    @PostMapping("/ke-hoach/{id}/duyet")
    @Operation(summary = "Duyệt kế hoạch triển khai")
    public ResponseEntity<ApiResponse<KeHoachTrienKhaiResponse>> duyetKeHoach(@PathVariable UUID id) {
        return ApiResponse.<KeHoachTrienKhaiResponse>build()
                .withData(tienDoService.duyetKeHoach(id))
                .withMessage("Đã duyệt kế hoạch")
                .toEntity();
    }

    @PostMapping("/ke-hoach/{id}/tu-choi")
    @Operation(summary = "Từ chối kế hoạch triển khai")
    public ResponseEntity<ApiResponse<KeHoachTrienKhaiResponse>> tuChoiKeHoach(
            @PathVariable UUID id,
            @RequestBody(required = false) KeHoachTuChoiRequest request) {
        return ApiResponse.<KeHoachTrienKhaiResponse>build()
                .withData(tienDoService.tuChoiKeHoach(id, request))
                .withMessage("Đã từ chối kế hoạch")
                .toEntity();
    }

    @PostMapping("/ke-hoach/{id}/import-chi-tiet")
    @Operation(summary = "Import chi tiết kế hoạch từ danh sách đối tượng")
    public ResponseEntity<ApiResponse<KeHoachTrienKhaiResponse>> importKeHoachChiTiet(
            @PathVariable UUID id,
            @Valid @RequestBody KeHoachImportChiTietRequest request) {
        return ApiResponse.<KeHoachTrienKhaiResponse>build()
                .withData(tienDoService.importKeHoachChiTiet(id, request))
                .withMessage("Đã import chi tiết kế hoạch")
                .toEntity();
    }

    @PostMapping("/vat-tu-yeu-cau")
    @Operation(summary = "Tạo phiếu yêu cầu vật tư")
    public ResponseEntity<ApiResponse<VatTuYeuCauResponse>> taoVatTuYeuCau(
            @Valid @RequestBody VatTuYeuCauTaoRequest request) {
        return ApiResponse.<VatTuYeuCauResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(tienDoService.taoVatTuYeuCau(request))
                .withMessage("Đã tạo phiếu yêu cầu vật tư")
                .toEntity();
    }

    @PutMapping("/vat-tu-yeu-cau/{id}/trang-thai")
    @Operation(summary = "Cập nhật trạng thái phiếu yêu cầu vật tư")
    public ResponseEntity<ApiResponse<VatTuYeuCauResponse>> capNhatTrangThaiVatTu(
            @PathVariable UUID id,
            @Valid @RequestBody VatTuTrangThaiCapNhatRequest request) {
        return ApiResponse.<VatTuYeuCauResponse>build()
                .withData(tienDoService.capNhatTrangThaiVatTu(id, request))
                .withMessage("Đã cập nhật trạng thái vật tư")
                .toEntity();
    }

    @GetMapping("/doi-tuong/{doiTuongId}/vat-tu")
    @Operation(summary = "Danh sách phiếu yêu cầu vật tư theo đối tượng")
    public ResponseEntity<ApiResponse<List<VatTuYeuCauResponse>>> listVatTuByDoiTuong(
            @PathVariable UUID doiTuongId) {
        return ApiResponse.<List<VatTuYeuCauResponse>>build()
                .withData(tienDoService.listVatTuByDoiTuong(doiTuongId))
                .toEntity();
    }

    @GetMapping("/kpi-nguong")
    @Operation(summary = "Danh sách cấu hình ngưỡng KPI")
    public ResponseEntity<ApiResponse<List<KpiNguongCauHinhResponse>>> listKpiNguong() {
        return ApiResponse.<List<KpiNguongCauHinhResponse>>build()
                .withData(tienDoService.listKpiNguong())
                .toEntity();
    }

    @PutMapping("/kpi-nguong/{id}")
    @Operation(summary = "Cập nhật cấu hình ngưỡng KPI")
    public ResponseEntity<ApiResponse<KpiNguongCauHinhResponse>> capNhatKpiNguong(
            @PathVariable UUID id,
            @Valid @RequestBody KpiNguongCapNhatRequest request) {
        return ApiResponse.<KpiNguongCauHinhResponse>build()
                .withData(tienDoService.capNhatKpiNguong(id, request))
                .withMessage("Đã cập nhật ngưỡng KPI")
                .toEntity();
    }
}
