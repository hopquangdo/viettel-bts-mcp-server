package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongLuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongLuongTrangThaiId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KieuHopDongLuongTrangThaiRepository extends JpaRepository<KieuHopDongLuongTrangThai, KieuHopDongLuongTrangThaiId> {

    Optional<KieuHopDongLuongTrangThai> findById_KieuHopDongIdAndId_LoaiHopDongIdAndNgayXoaIsNull(
            UUID kieuHopDongId, UUID loaiHopDongId);

    @Query("""
            SELECT k FROM KieuHopDongLuongTrangThai k
            JOIN LuongTrangThai l ON l.id = k.luongTrangThaiId
            WHERE (:includeDeleted = TRUE OR k.ngayXoa IS NULL)
              AND (:includeDeleted = TRUE OR l.ngayXoa IS NULL)
              AND (:loaiHopDongId IS NULL OR k.id.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR k.id.kieuHopDongId = :kieuHopDongId)
            """)
    List<KieuHopDongLuongTrangThai> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    List<KieuHopDongLuongTrangThai> findByLuongTrangThaiIdAndNgayXoaIsNull(UUID luongTrangThaiId);
}
