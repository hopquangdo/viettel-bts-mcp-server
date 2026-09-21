package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMapping;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExcelMappingRepository extends JpaRepository<ExcelMapping, UUID> {

    List<ExcelMapping> findByNgayXoaIsNull();

    Optional<ExcelMapping> findByIdAndNgayXoaIsNull(UUID id);

    List<ExcelMapping> findByDoiTuongQuanLyIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID doiTuongQuanLyId);

    @Query("""
            SELECT e FROM ExcelMapping e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (:doiTuongQuanLyId IS NULL OR e.doiTuongQuanLyId = :doiTuongQuanLyId)
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.ma, ''), ' ',
                        COALESCE(e.moTa, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<ExcelMapping> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("keyword") String keyword);
}
