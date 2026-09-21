package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachHopDongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachMoRongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTinhChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTramRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongDanhSachService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong/danh-sach")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.HopDong.TAG, description = ApiEntityCatalog.HopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class HopDongDanhSachController {

    private final HopDongDanhSachService hopDongDanhSachService;

    @GetMapping("/tong-quan")
    @Operation(summary = "Tổng quan danh sách hợp đồng (loại, kiểu, luồng trạng thái)")
    public ResponseEntity<ApiResponse<HopDongDanhSachTongQuanResponse>> getTongQuan(
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchive) {
        return ApiResponse.<HopDongDanhSachTongQuanResponse>build()
                .withData(hopDongDanhSachService.getTongQuan(loaiHopDongId, includeArchive))
                .toEntity();
    }

    @GetMapping("/hop-dong")
    @Operation(summary = "Danh sách hợp đồng active theo kiểu HĐ (giá trị, tiến độ luồng, thuộc tính động)")
    public ResponseEntity<ApiResponse<PageResponse<HopDongDanhSachHopDongItemResponse>>> listHopDong(
            @RequestParam(required = false) UUID kieuHopDongId,
            @RequestParam(required = false) UUID loaiHopDongId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchive) {
        return ApiResponse.<PageResponse<HopDongDanhSachHopDongItemResponse>>build()
                .withData(hopDongDanhSachService.listHopDong(
                        kieuHopDongId, loaiHopDongId, search, page, size, includeArchive))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/mo-rong")
    @Operation(summary = "Dữ liệu mở rộng hợp đồng: nhóm ưu tiên + đối tượng QL theo khu vực/tỉnh")
    public ResponseEntity<ApiResponse<HopDongDanhSachMoRongResponse>> getMoRong(@PathVariable UUID hopDongId) {
        return ApiResponse.<HopDongDanhSachMoRongResponse>build()
                .withData(hopDongDanhSachService.getMoRong(hopDongId))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/tinh-chi-tiet")
    @Operation(summary = "Chi tiết trạm theo tỉnh/khu vực/nhà thầu trong hợp đồng")
    public ResponseEntity<ApiResponse<HopDongDanhSachTinhChiTietResponse>> getTinhChiTiet(
            @PathVariable UUID hopDongId,
            @RequestParam(required = false) String tinh,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String nhaThau) {
        return ApiResponse.<HopDongDanhSachTinhChiTietResponse>build()
                .withData(hopDongDanhSachService.getTinhChiTiet(hopDongId, khuVuc, tinh, nhaThau))
                .toEntity();
    }

    @GetMapping("/hop-dong/{hopDongId}/danh-sach-tram")
    @Operation(summary = "Toàn bộ trạm của 1 hợp đồng (mọi tỉnh) — dùng cho trang Biên bản & hồ sơ theo trạm")
    public ResponseEntity<ApiResponse<List<HopDongDanhSachTramRowResponse>>> getDanhSachTram(@PathVariable UUID hopDongId) {
        return ApiResponse.<List<HopDongDanhSachTramRowResponse>>build()
                .withData(hopDongDanhSachService.getDanhSachTramHopDong(hopDongId))
                .toEntity();
    }
}
