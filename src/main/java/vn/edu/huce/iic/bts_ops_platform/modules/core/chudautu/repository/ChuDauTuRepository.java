package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.entity.ChuDauTu;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChuDauTuRepository extends JpaRepository<ChuDauTu, UUID> {

    List<ChuDauTu> findByNgayXoaIsNull();

    Optional<ChuDauTu> findByIdAndNgayXoaIsNull(UUID id);

    boolean existsByMaIgnoreCase(String ma);
    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);
}
