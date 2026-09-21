package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuyenRepository extends JpaRepository<Quyen, UUID> {

    List<Quyen> findByNgayXoaIsNull();

    Optional<Quyen> findByIdAndNgayXoaIsNull(UUID id);

    Optional<Quyen> findByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);
}
