package vn.edu.huce.iic.bts_ops_platform.modules.auditlog;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;

public interface AuditLogService {

    PageResponse<AuditLogResponse> list(Integer page, Integer size);

    /** Lịch sử thao tác liên quan tới 1 trạm/đối tượng (màn Lịch sử chỉnh sửa trạm). */
    PageResponse<AuditLogResponse> listByDoiTuong(java.util.UUID doiTuongId, Integer page, Integer size);
}
