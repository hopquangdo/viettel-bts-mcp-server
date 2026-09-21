package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhanCongRepository extends JpaRepository<PhanCong, UUID> {

    List<PhanCong> findByNgayXoaIsNull();

    Optional<PhanCong> findByIdAndNgayXoaIsNull(UUID id);

    List<PhanCong> findByHopDongDoiTuongIdInAndNgayXoaIsNull(Collection<UUID> hopDongDoiTuongIds);

    List<PhanCong> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    @Query("""
            SELECT p FROM PhanCong p
            WHERE p.ngayXoa IS NULL
              AND (
                p.nguoiDungId = :nguoiDungId
                OR lower(trim(p.nhaThau)) = lower(trim(:hoTen))
                OR lower(trim(p.nhaThau)) = lower(trim(:tenDangNhap))
              )
            """)
    List<PhanCong> findActiveForContractor(
            @Param("nguoiDungId") UUID nguoiDungId,
            @Param("hoTen") String hoTen,
            @Param("tenDangNhap") String tenDangNhap);
}
