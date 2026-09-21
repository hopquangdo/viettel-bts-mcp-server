package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongHopDongLienKet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.UUID;

public interface DoiTuongHopDongLienKetRepository extends JpaRepository<DoiTuongHopDongLienKet, UUID> {

    List<DoiTuongHopDongLienKet> findByNgayXoaIsNull();

    List<DoiTuongHopDongLienKet> findByLoaiHopDongIdAndKieuHopDongIdIsNullAndNgayXoaIsNull(UUID loaiHopDongId);

    List<DoiTuongHopDongLienKet> findByKieuHopDongIdAndNgayXoaIsNull(UUID kieuHopDongId);

    @Query("""
            SELECT lk FROM DoiTuongHopDongLienKet lk
            WHERE lk.ngayXoa IS NULL
              AND (
                lk.kieuHopDongId = :kieuHopDongId
                OR (lk.loaiHopDongId = :loaiHopDongId AND lk.kieuHopDongId IS NULL)
              )
            """)
    List<DoiTuongHopDongLienKet> findEffectiveForKieu(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    @Query("""
            SELECT lk FROM DoiTuongHopDongLienKet lk
            WHERE """
            + CauHinhListQuery.NOT_DELETED_LK + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY_LK + """
              AND (
                    (:scopeKieu = TRUE AND (
                        lk.kieuHopDongId = :kieuHopDongId
                        OR (lk.loaiHopDongId = :loaiHopDongId AND lk.kieuHopDongId IS NULL)
                    ))
                    OR (:scopeLoaiOnly = TRUE AND lk.loaiHopDongId = :loaiHopDongId AND lk.kieuHopDongId IS NULL)
                    OR (:scopeAll = TRUE)
                  )
            ORDER BY lk.thuTu ASC, lk.ngayTao ASC
            """)
    List<DoiTuongHopDongLienKet> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("scopeKieu") boolean scopeKieu,
            @Param("scopeLoaiOnly") boolean scopeLoaiOnly,
            @Param("scopeAll") boolean scopeAll);
}
