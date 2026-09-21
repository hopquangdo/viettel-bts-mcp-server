package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.MoTaTinh;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoTaTinhRepository extends JpaRepository<MoTaTinh, UUID> {

    Optional<MoTaTinh> findByTinhThanhIdAndNgayXoaIsNull(UUID tinhThanhId);

    List<MoTaTinh> findByNgayXoaIsNull();
}
