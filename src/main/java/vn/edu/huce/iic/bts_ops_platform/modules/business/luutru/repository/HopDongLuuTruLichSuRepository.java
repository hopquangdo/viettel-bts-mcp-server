package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTruLichSu;

import java.util.List;
import java.util.UUID;

public interface HopDongLuuTruLichSuRepository extends JpaRepository<HopDongLuuTruLichSu, UUID> {

    List<HopDongLuuTruLichSu> findByHopDongIdOrderByNgayTaoDesc(UUID hopDongId);
}
