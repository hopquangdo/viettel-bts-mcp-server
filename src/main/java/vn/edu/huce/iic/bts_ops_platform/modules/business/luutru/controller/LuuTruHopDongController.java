package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.LuuTruHopDongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/luu-tru/hop-dong")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.LuuTruHopDong.TAG, description = ApiEntityCatalog.LuuTruHopDong.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_LUU_TRU)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class LuuTruHopDongController {

    private final LuuTruHopDongService luuTruHopDongService;

    @GetMapping
    @Operation(summary = "Danh sách hợp đồng và trạng thái lưu trữ")
    public ResponseEntity<ApiResponse<List<HopDongLuuTruResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.<List<HopDongLuuTruResponse>>build()
                .withData(luuTruHopDongService.list(search, activeOnly, includeDeleted))
                .toEntity();
    }

    @PostMapping("/{hopDongId}/archive")
    @Operation(summary = "Archive hợp đồng (yêu cầu 100% hoàn thành)")
    public ResponseEntity<ApiResponse<HopDongLuuTruResponse>> archive(@PathVariable UUID hopDongId) {
        return ApiResponse.<HopDongLuuTruResponse>build()
                .withData(luuTruHopDongService.archive(hopDongId))
                .withMessage("Đã Archive hợp đồng")
                .toEntity();
    }

    @PostMapping("/{hopDongId}/khoi-phuc")
    @Operation(summary = "Khôi phục hợp đồng đã Archive")
    public ResponseEntity<ApiResponse<HopDongLuuTruResponse>> khoiPhuc(@PathVariable UUID hopDongId) {
        return ApiResponse.<HopDongLuuTruResponse>build()
                .withData(luuTruHopDongService.khoiPhuc(hopDongId))
                .withMessage("Đã khôi phục hợp đồng")
                .toEntity();
    }

    @GetMapping("/{hopDongId}/lich-su")
    @Operation(summary = "Lịch sử Archive / khôi phục hợp đồng")
    public ResponseEntity<ApiResponse<List<HopDongLuuTruLichSuResponse>>> lichSu(@PathVariable UUID hopDongId) {
        return ApiResponse.<List<HopDongLuuTruLichSuResponse>>build()
                .withData(luuTruHopDongService.lichSu(hopDongId))
                .toEntity();
    }
}
