package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhKieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThuocTinhKieuHopDongRepository extends JpaRepository<ThuocTinhKieuHopDong, UUID> {

    List<ThuocTinhKieuHopDong> findByNgayXoaIsNull();

    Optional<ThuocTinhKieuHopDong> findByIdAndNgayXoaIsNull(UUID id);

    List<ThuocTinhKieuHopDong> findByKieuHopDongIdAndNgayXoaIsNullOrderByThuTuAsc(UUID kieuHopDongId);

    @Query("""
            SELECT lk FROM ThuocTinhKieuHopDong lk
            WHERE lk.kieuHopDongId = :kieuHopDongId
              AND """
            + CauHinhListQuery.NOT_DELETED_LK + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY_LK + """
              AND EXISTS (
                  SELECT 1 FROM ThuocTinhHopDong attr
                  WHERE attr.id = lk.thuocTinhHopDongId
                    AND (:includeDeleted = TRUE OR attr.ngayXoa IS NULL)
              )
            ORDER BY lk.thuTu ASC
            """)
    List<ThuocTinhKieuHopDong> search(
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly);
}
