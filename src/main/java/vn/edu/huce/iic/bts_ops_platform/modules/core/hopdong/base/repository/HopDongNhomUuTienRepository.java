package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongNhomUuTienRepository extends JpaRepository<HopDongNhomUuTien, UUID> {

    List<HopDongNhomUuTien> findByNgayXoaIsNull();

    Optional<HopDongNhomUuTien> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongNhomUuTien> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    Optional<HopDongNhomUuTien> findFirstByHopDongIdAndTenIgnoreCaseAndNgayXoaIsNull(UUID hopDongId, String ten);
    List<HopDongNhomUuTien> findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscNgayTaoAsc(UUID hopDongId);
}
