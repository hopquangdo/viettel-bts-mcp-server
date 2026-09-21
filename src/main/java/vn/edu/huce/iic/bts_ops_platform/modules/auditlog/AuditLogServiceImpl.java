package vn.edu.huce.iic.bts_ops_platform.modules.auditlog;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.modules.auditlog.entity.AuditLog;
import vn.edu.huce.iic.bts_ops_platform.modules.auditlog.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(Integer page, Integer size) {
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.AUDIT_LOG_DEFAULT_SIZE, PaginationDefaults.AUDIT_LOG_MAX_SIZE);
        Page<AuditLog> pageResult = auditLogRepository.findAllByOrderByNgayTaoDesc(PageRequest.of(pageNumber, pageSize));
        return PageResponse.ofItems(
                pageResult.getContent().stream().map(this::toResponse).toList(),
                pageNumber, pageSize, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listByDoiTuong(java.util.UUID doiTuongId, Integer page, Integer size) {
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.AUDIT_LOG_DEFAULT_SIZE, PaginationDefaults.AUDIT_LOG_MAX_SIZE);
        Page<AuditLog> pageResult = auditLogRepository.findByDoiTuongIdOrderByNgayTaoDesc(doiTuongId, PageRequest.of(pageNumber, pageSize));
        return PageResponse.ofItems(
                pageResult.getContent().stream().map(this::toResponse).toList(),
                pageNumber, pageSize, pageResult.getTotalElements());
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .nguoiThucHienId(entity.getNguoiThucHienId())
                .tenNguoiThucHien(entity.getTenNguoiThucHien())
                .hanhDong(entity.getHanhDong())
                .moTa(entity.getMoTa())
                .ip(entity.getIp())
                .doiTuongId(entity.getDoiTuongId())
                .hopDongId(entity.getHopDongId())
                .ngayTao(entity.getNgayTao())
                .build();
    }
}
