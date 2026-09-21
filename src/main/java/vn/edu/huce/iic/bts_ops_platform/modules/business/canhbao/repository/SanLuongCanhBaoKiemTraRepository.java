package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.SanLuongCanhBaoKiemTra;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SanLuongCanhBaoKiemTraRepository extends JpaRepository<SanLuongCanhBaoKiemTra, UUID> {

    Optional<SanLuongCanhBaoKiemTra> findByHopDongDoiTuongId(UUID hopDongDoiTuongId);

    List<SanLuongCanhBaoKiemTra> findByHopDongDoiTuongIdIn(Collection<UUID> hopDongDoiTuongIds);
}
