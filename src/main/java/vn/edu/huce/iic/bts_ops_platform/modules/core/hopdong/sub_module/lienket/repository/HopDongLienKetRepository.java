package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.entity.HopDongLienKet;

import java.util.List;
import java.util.UUID;

public interface HopDongLienKetRepository extends JpaRepository<HopDongLienKet, UUID> {

    List<HopDongLienKet> findByHopDongIdAndNgayXoaIsNullOrderByNgayTaoAsc(UUID hopDongId);

    List<HopDongLienKet> findByNgayXoaIsNullOrderByNgayTaoDesc();
}
