package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KeHoachTrienKhaiChiTiet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeHoachTrienKhaiChiTietRepository extends JpaRepository<KeHoachTrienKhaiChiTiet, UUID> {

    List<KeHoachTrienKhaiChiTiet> findByKeHoachIdAndNgayXoaIsNull(UUID keHoachId);

    Optional<KeHoachTrienKhaiChiTiet> findByKeHoachIdAndHopDongDoiTuongIdAndNgayXoaIsNull(
            UUID keHoachId, UUID hopDongDoiTuongId);
}
