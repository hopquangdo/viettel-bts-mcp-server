package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoBatchSoLanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDanhSachItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDeXuatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.LanChinhSuaGanNhatResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.SoLanSuaThongSoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.service.ChinhSuaThongSoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chinh-sua-thong-so")
@RequiredArgsConstructor
@Tag(name = "ChinhSuaThongSo", description = "Kiểm soát chỉnh sửa thông số trạm — đếm lần sửa, giải trình, phê duyệt")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ChinhSuaThongSoController {

    private final ChinhSuaThongSoService chinhSuaThongSoService;

    @PostMapping("/doi-tuong/so-lan-batch")
    @Operation(summary = "Số lần sửa từng thuộc tính — batch nhiều trạm")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
    public ResponseEntity<ApiResponse<List<SoLanSuaThongSoResponse>>> demSoLanBatch(
            @Valid @RequestBody ChinhSuaThongSoBatchSoLanRequest request) {
        return ApiResponse.<List<SoLanSuaThongSoResponse>>build()
                .withData(chinhSuaThongSoService.demSoLanDaApDungBatchList(request.getHopDongDoiTuongIds()))
                .toEntity();
    }

    @PostMapping("/doi-tuong/lan-chinh-sua-gan-nhat-batch")
    @Operation(summary = "Thời điểm chỉnh sửa thông số gần nhất — batch nhiều trạm")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
    public ResponseEntity<ApiResponse<List<LanChinhSuaGanNhatResponse>>> lanChinhSuaGanNhatBatch(
            @Valid @RequestBody ChinhSuaThongSoBatchSoLanRequest request) {
        return ApiResponse.<List<LanChinhSuaGanNhatResponse>>build()
                .withData(chinhSuaThongSoService.lanChinhSuaGanNhatBatch(request.getHopDongDoiTuongIds()))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{doiTuongId}/so-lan")
    @Operation(summary = "Số lần đã sửa từng thuộc tính của 1 trạm")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
    public ResponseEntity<ApiResponse<SoLanSuaThongSoResponse>> demSoLan(@PathVariable UUID doiTuongId) {
        return ApiResponse.<SoLanSuaThongSoResponse>build()
                .withData(chinhSuaThongSoService.demSoLanDaApDung(doiTuongId))
                .toEntity();
    }

    @GetMapping("/doi-tuong/{doiTuongId}/lich-su")
    @Operation(summary = "Lịch sử chỉnh sửa thông số (kèm giải trình)")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
    public ResponseEntity<ApiResponse<List<ChinhSuaThongSoResponse>>> lichSu(@PathVariable UUID doiTuongId) {
        return ApiResponse.<List<ChinhSuaThongSoResponse>>build()
                .withData(chinhSuaThongSoService.lichSu(doiTuongId))
                .toEntity();
    }

    @PostMapping("/de-xuat")
    @Operation(summary = "Đề xuất chỉnh sửa — tự áp dụng lần 1–2, chờ duyệt từ lần 3")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI)
    public ResponseEntity<ApiResponse<List<ChinhSuaThongSoResponse>>> deXuat(
            @Valid @RequestBody ChinhSuaThongSoDeXuatRequest request) {
        return ApiResponse.<List<ChinhSuaThongSoResponse>>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(chinhSuaThongSoService.deXuat(request))
                .withMessage("Đã gửi đề xuất chỉnh sửa")
                .toEntity();
    }

    @GetMapping("/cho-duyet")
    @Operation(summary = "Danh sách đề xuất chờ quản lý phê duyệt")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    public ResponseEntity<ApiResponse<PageResponse<ChinhSuaThongSoDanhSachItemResponse>>> danhSachChoDuyet(
            @RequestParam(required = false) UUID hopDongId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<ChinhSuaThongSoDanhSachItemResponse>>build()
                .withData(chinhSuaThongSoService.danhSachChoDuyet(hopDongId, page, size))
                .toEntity();
    }

    @PostMapping("/{id}/phe-duyet")
    @Operation(summary = "Phê duyệt đề xuất chỉnh sửa thông số")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    public ResponseEntity<ApiResponse<ChinhSuaThongSoResponse>> pheDuyet(@PathVariable UUID id) {
        return ApiResponse.<ChinhSuaThongSoResponse>build()
                .withData(chinhSuaThongSoService.pheDuyet(id))
                .withMessage("Đã phê duyệt chỉnh sửa thông số")
                .toEntity();
    }

    @PostMapping("/{id}/tu-choi")
    @Operation(summary = "Từ chối đề xuất chỉnh sửa thông số")
    @RequiresPermission(QuyenHanMa.SUA_SO_LIEU_DA_DUYET)
    public ResponseEntity<ApiResponse<ChinhSuaThongSoResponse>> tuChoi(
            @PathVariable UUID id,
            @Valid @RequestBody ChinhSuaThongSoTuChoiRequest request) {
        return ApiResponse.<ChinhSuaThongSoResponse>build()
                .withData(chinhSuaThongSoService.tuChoi(id, request.getLyDo()))
                .withMessage("Đã từ chối đề xuất")
                .toEntity();
    }
}
