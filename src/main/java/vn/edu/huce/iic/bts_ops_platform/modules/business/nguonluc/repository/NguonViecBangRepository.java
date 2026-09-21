package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.entity.NguonViecBang;

import java.util.Optional;
import java.util.UUID;

public interface NguonViecBangRepository extends JpaRepository<NguonViecBang, UUID> {

    Optional<NguonViecBang> findFirstByTrungTamIgnoreCaseAndNgayXoaIsNull(String trungTam);
}
