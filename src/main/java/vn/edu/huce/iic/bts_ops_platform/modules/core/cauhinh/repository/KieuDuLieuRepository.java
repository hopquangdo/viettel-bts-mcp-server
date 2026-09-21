package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KieuDuLieuRepository extends JpaRepository<KieuDuLieu, UUID> {

    List<KieuDuLieu> findByNgayXoaIsNull();

    Optional<KieuDuLieu> findByIdAndNgayXoaIsNull(UUID id);

    Optional<KieuDuLieu> findByLienKetBangAndNgayXoaIsNull(String lienKetBang);

    Optional<KieuDuLieu> findByTenAndNgayXoaIsNull(String ten);

    @Query("""
            SELECT e FROM KieuDuLieu e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.lienKetBang, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<KieuDuLieu> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("keyword") String keyword);
}
