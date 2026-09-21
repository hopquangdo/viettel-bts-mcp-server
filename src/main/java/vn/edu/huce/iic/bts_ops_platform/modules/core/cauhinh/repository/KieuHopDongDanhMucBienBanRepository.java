package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongDanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KieuHopDongDanhMucBienBanRepository extends JpaRepository<KieuHopDongDanhMucBienBan, UUID> {

    List<KieuHopDongDanhMucBienBan> findByKieuHopDongIdAndHoatDongTrueAndNgayXoaIsNull(UUID kieuHopDongId);

    List<KieuHopDongDanhMucBienBan> findByKieuHopDongIdAndNgayXoaIsNull(UUID kieuHopDongId);

    List<KieuHopDongDanhMucBienBan> findByKieuHopDongIdAndNgayXoaIsNullOrderByThuTuAsc(UUID kieuHopDongId);

    Optional<KieuHopDongDanhMucBienBan> findByIdAndNgayXoaIsNull(UUID id);

    @Query("""
            SELECT lk FROM KieuHopDongDanhMucBienBan lk
            WHERE lk.kieuHopDongId = :kieuHopDongId
              AND """
            + CauHinhListQuery.NOT_DELETED_LK + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY_LK + """
              AND EXISTS (
                  SELECT 1 FROM DanhMucBienBan dm
                  WHERE dm.id = lk.danhMucBienBanId
                    AND (:includeDeleted = TRUE OR dm.ngayXoa IS NULL)
              )
            ORDER BY lk.thuTu ASC
            """)
    List<KieuHopDongDanhMucBienBan> search(
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly);
}
