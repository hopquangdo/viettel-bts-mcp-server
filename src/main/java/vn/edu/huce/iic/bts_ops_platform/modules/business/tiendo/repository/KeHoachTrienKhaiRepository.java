package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KeHoachTrienKhai;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeHoachTrienKhaiRepository extends JpaRepository<KeHoachTrienKhai, UUID> {

    List<KeHoachTrienKhai> findByNgayXoaIsNullOrderByNgayTaoDesc();

    List<KeHoachTrienKhai> findByHopDongIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID hopDongId);

    Optional<KeHoachTrienKhai> findByIdAndNgayXoaIsNull(UUID id);
}
