package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinhTepDinhKem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BienBanPhatSinhTepDinhKemRepository extends JpaRepository<BienBanPhatSinhTepDinhKem, UUID> {

    List<BienBanPhatSinhTepDinhKem> findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(Collection<UUID> bienBanPhatSinhIds);
}
