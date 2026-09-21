package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.VatTuYeuCau;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VatTuYeuCauRepository extends JpaRepository<VatTuYeuCau, UUID> {

    List<VatTuYeuCau> findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID hopDongDoiTuongId);

    Optional<VatTuYeuCau> findByIdAndNgayXoaIsNull(UUID id);
}
