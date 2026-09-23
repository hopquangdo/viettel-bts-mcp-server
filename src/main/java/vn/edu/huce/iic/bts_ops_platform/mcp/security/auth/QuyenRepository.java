package vn.edu.huce.iic.bts_ops_platform.mcp.security.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QuyenRepository extends JpaRepository<Quyen, UUID> {
    Optional<Quyen> findByIdAndNgayXoaIsNull(UUID id);
}
