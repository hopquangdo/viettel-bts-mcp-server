package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.TramThiCongBuoc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TramThiCongBuocRepository extends JpaRepository<TramThiCongBuoc, UUID> {

    List<TramThiCongBuoc> findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByMaBuocAsc(UUID hopDongDoiTuongId);

    Optional<TramThiCongBuoc> findByHopDongDoiTuongIdAndMaBuocAndNgayXoaIsNull(UUID hopDongDoiTuongId, String maBuoc);
}
