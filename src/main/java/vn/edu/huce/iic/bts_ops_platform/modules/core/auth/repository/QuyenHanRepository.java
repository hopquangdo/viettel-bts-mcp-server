package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.QuyenHan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuyenHanRepository extends JpaRepository<QuyenHan, UUID> {

    List<QuyenHan> findByNgayXoaIsNull();

    Optional<QuyenHan> findByIdAndNgayXoaIsNull(UUID id);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);
}
