package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoUnreadCountResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thong-bao")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.ThongBao.TAG, description = ApiEntityCatalog.ThongBao.DESCRIPTION)
@RequiresPermission(anyOf = {
        QuyenHanMa.QUAN_LY_HOP_DONG,
        QuyenHanMa.QUAN_LY_SAN_LUONG,
        QuyenHanMa.XEM_NHIEM_VU_NHA_THAU,
        QuyenHanMa.QUAN_LY_PHAN_CONG
})
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo của người dùng hiện tại")
    public ResponseEntity<ApiResponse<PageResponse<ThongBaoResponse>>> inbox(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<ThongBaoResponse>>build()
                .withData(thongBaoService.inbox(page, size))
                .toEntity();
    }

    @GetMapping("/gan-day")
    @Operation(summary = "Danh sách thông báo gần đây (giới hạn số lượng)")
    public ResponseEntity<ApiResponse<List<ThongBaoResponse>>> listRecent(
            @RequestParam(required = false, defaultValue = "50") int limit) {
        return ApiResponse.<List<ThongBaoResponse>>build()
                .withData(thongBaoService.listMine(limit))
                .toEntity();
    }

    @GetMapping("/dem-chua-doc")
    @Operation(summary = "Số thông báo chưa đọc (định dạng FE)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> demChuaDoc() {
        return ApiResponse.<Map<String, Long>>build()
                .withData(Map.of("soLuong", thongBaoService.demChuaDoc()))
                .toEntity();
    }

    @GetMapping("/chua-doc/count")
    @Operation(summary = "Số thông báo chưa đọc")
    public ResponseEntity<ApiResponse<ThongBaoUnreadCountResponse>> unreadCount() {
        return ApiResponse.<ThongBaoUnreadCountResponse>build()
                .withData(thongBaoService.unreadCount())
                .toEntity();
    }

    @PatchMapping("/{id}/doc")
    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    public ResponseEntity<ApiResponse<ThongBaoResponse>> markRead(@PathVariable UUID id) {
        return ApiResponse.<ThongBaoResponse>build()
                .withData(thongBaoService.danhDauDaDoc(id))
                .withMessage("Đã đọc")
                .toEntity();
    }

    @PatchMapping("/doc-tat-ca")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        thongBaoService.markAllRead();
        return ApiResponse.<Void>build().withMessage("Đã đọc tất cả").toEntity();
    }
}
