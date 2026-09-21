package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.HopDongDoiTuongChecklistDapUng;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongDoiTuongChecklistDapUngRepository extends JpaRepository<HopDongDoiTuongChecklistDapUng, UUID> {

    List<HopDongDoiTuongChecklistDapUng> findByHopDongDoiTuongIdInAndNgayXoaIsNull(Collection<UUID> hopDongDoiTuongIds);

    Optional<HopDongDoiTuongChecklistDapUng> findByHopDongDoiTuongIdAndChecklistMucIdAndNgayXoaIsNull(
            UUID hopDongDoiTuongId,
            UUID checklistMucId);
}
