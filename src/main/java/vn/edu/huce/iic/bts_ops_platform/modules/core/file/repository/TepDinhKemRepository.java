package vn.edu.huce.iic.bts_ops_platform.modules.core.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.entity.TepDinhKem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TepDinhKemRepository extends JpaRepository<TepDinhKem, UUID> {

    List<TepDinhKem> findByNgayXoaIsNull();

    Optional<TepDinhKem> findByIdAndNgayXoaIsNull(UUID id);

    List<TepDinhKem> findByIdInAndNgayXoaIsNull(List<UUID> ids);
}
