package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoiTuongQuanLyRepository extends JpaRepository<DoiTuongQuanLy, UUID> {

    List<DoiTuongQuanLy> findByNgayXoaIsNull();

    Optional<DoiTuongQuanLy> findByIdAndNgayXoaIsNull(UUID id);

    @Query("""
            SELECT e FROM DoiTuongQuanLy e
            WHERE e.id IN :ids
              AND e.ngayXoa IS NULL
            """)
    List<DoiTuongQuanLy> findActiveByIdIn(@Param("ids") Collection<UUID> ids);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT e FROM DoiTuongQuanLy e
            WHERE (:includeDeleted = TRUE OR e.ngayXoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR e.hoatDong = TRUE)
              AND (
                    :selectorOnly IS NULL
                    OR :selectorOnly = FALSE
                    OR COALESCE(e.hienThiTrenGiaoDien, TRUE) = TRUE
                  )
              AND (:filterByIds = FALSE OR e.id IN :allowedIds)
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ma, ''), ' ',
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.bieuTuong, ''), ' ',
                        COALESCE(e.moTa, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<DoiTuongQuanLy> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("selectorOnly") Boolean selectorOnly,
            @Param("filterByIds") boolean filterByIds,
            @Param("allowedIds") Collection<UUID> allowedIds,
            @Param("keyword") String keyword);
}
