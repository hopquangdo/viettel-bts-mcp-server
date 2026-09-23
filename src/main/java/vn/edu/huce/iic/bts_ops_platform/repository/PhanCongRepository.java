package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.entity.phancong.PhanCong;
import java.util.List;
import java.util.UUID;

public interface PhanCongRepository extends JpaRepository<PhanCong, UUID> {
    @Query("select p from PhanCong p where p.ngayXoa is null and (p.nguoiDungId = :nguoiDungId or lower(trim(p.nhaThau)) = lower(trim(:hoTen)) or lower(trim(p.nhaThau)) = lower(trim(:tenDangNhap)))")
    List<PhanCong> findActiveForContractor(@Param("nguoiDungId") UUID nguoiDungId,
                                            @Param("hoTen") String hoTen,
                                            @Param("tenDangNhap") String tenDangNhap);
}
