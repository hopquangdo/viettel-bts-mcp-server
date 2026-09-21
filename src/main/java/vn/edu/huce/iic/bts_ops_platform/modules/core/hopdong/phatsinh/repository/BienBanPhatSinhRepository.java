package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;

import java.util.List;
import java.util.UUID;

public interface BienBanPhatSinhRepository extends JpaRepository<BienBanPhatSinh, UUID> {

    List<BienBanPhatSinh> findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(UUID hopDongId);

    long countByHopDongIdAndNgayXoaIsNull(UUID hopDongId);
}
