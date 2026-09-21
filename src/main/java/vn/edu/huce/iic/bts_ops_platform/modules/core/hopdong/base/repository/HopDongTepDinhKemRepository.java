package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongTepDinhKemRepository extends JpaRepository<HopDongTepDinhKem, UUID> {

    List<HopDongTepDinhKem> findByNgayXoaIsNull();

    Optional<HopDongTepDinhKem> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongTepDinhKem> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);
}
