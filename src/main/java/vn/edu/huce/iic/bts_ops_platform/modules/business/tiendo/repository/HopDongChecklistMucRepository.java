package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.HopDongChecklistMuc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongChecklistMucRepository extends JpaRepository<HopDongChecklistMuc, UUID> {

    List<HopDongChecklistMuc> findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscTenAsc(UUID hopDongId);

    Optional<HopDongChecklistMuc> findByIdAndNgayXoaIsNull(UUID id);
}
