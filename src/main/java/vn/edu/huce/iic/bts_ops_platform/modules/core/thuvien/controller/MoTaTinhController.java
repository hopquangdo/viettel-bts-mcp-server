package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.MoTaTinhRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.MoTaTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.MoTaTinhService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mo-ta-tinh")
@RequiredArgsConstructor
@Tag(name = "Mô tả tỉnh", description = "Mô tả địa lý/khí hậu/kinh tế theo tỉnh — dùng điền tự động Thuyết minh khảo sát")
@RequiresPermission(QuyenHanMa.QUAN_LY_TINH_THANH)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class MoTaTinhController {

    private final MoTaTinhService moTaTinhService;

    @GetMapping
    @Operation(summary = "Danh sách tỉnh/thành kèm mô tả (nếu đã có)")
    public ResponseEntity<ApiResponse<List<MoTaTinhResponse>>> list() {
        return ApiResponse.<List<MoTaTinhResponse>>build()
                .withData(moTaTinhService.list())
                .toEntity();
    }

    @PutMapping("/{tinhThanhId}")
    @Operation(summary = "Thêm/cập nhật mô tả cho 1 tỉnh")
    public ResponseEntity<ApiResponse<MoTaTinhResponse>> upsert(
            @PathVariable UUID tinhThanhId, @Valid @RequestBody MoTaTinhRequest request) {
        return ApiResponse.<MoTaTinhResponse>build()
                .withData(moTaTinhService.upsert(tinhThanhId, request))
                .withMessage("Đã lưu mô tả tỉnh")
                .toEntity();
    }
}
