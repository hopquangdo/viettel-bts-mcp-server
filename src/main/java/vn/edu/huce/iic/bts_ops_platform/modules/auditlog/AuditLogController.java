package vn.edu.huce.iic.bts_ops_platform.modules.auditlog;

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

@RestController
@RequestMapping("/api/v1/audit-log")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.AuditLog.TAG, description = ApiEntityCatalog.AuditLog.DESCRIPTION)
@RequiresPermission(QuyenHanMa.QUAN_LY_AUDIT_LOG)
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Danh sách audit log (phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<AuditLogResponse>>build()
                .withData(auditLogService.list(page, size))
                .toEntity();
    }

    /** Lịch sử thao tác của 1 trạm — quyền theo đối tượng hợp đồng (không đòi quyền audit toàn hệ),
     * vì màn "Lịch sử chỉnh sửa trạm" là của người vận hành danh sách trạm. */
    @GetMapping("/doi-tuong/{doiTuongId}")
    @RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
    @Operation(summary = "Lịch sử thao tác liên quan tới 1 trạm/đối tượng (phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> listByDoiTuong(
            @PathVariable java.util.UUID doiTuongId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<AuditLogResponse>>build()
                .withData(auditLogService.listByDoiTuong(doiTuongId, page, size))
                .toEntity();
    }
}
