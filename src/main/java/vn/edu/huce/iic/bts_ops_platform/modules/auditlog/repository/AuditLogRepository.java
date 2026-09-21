package vn.edu.huce.iic.bts_ops_platform.modules.auditlog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.auditlog.entity.AuditLog;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByOrderByNgayTaoDesc(Pageable pageable);

    Page<AuditLog> findByDoiTuongIdOrderByNgayTaoDesc(UUID doiTuongId, Pageable pageable);
}
