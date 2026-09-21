package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, UUID> {

    List<NguoiDung> findByNgayXoaIsNull();

    Optional<NguoiDung> findByIdAndNgayXoaIsNull(UUID id);

    List<NguoiDung> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    Optional<NguoiDung> findByTenDangNhapIgnoreCase(String tenDangNhap);

    boolean existsByTenDangNhapIgnoreCase(String tenDangNhap);

    List<NguoiDung> findByQuyenIdAndIdNotOrderByHoTenAsc(UUID quyenId, UUID excludeId);

    List<NguoiDung> findByKhuVucIdAndIdNotOrderByHoTenAsc(UUID khuVucId, UUID excludeId);

    List<NguoiDung> findByQuyenIdInAndNgayXoaIsNullAndHoatDongTrue(Collection<UUID> quyenIds);

    List<NguoiDung> findByHoTenContainingIgnoreCaseAndNgayXoaIsNull(String hoTen);
}
