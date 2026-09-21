package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinh;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

public interface ThuocTinhRepository extends JpaRepository<ThuocTinh, UUID> {

    List<ThuocTinh> findByNgayXoaIsNull();

    Optional<ThuocTinh> findByIdAndNgayXoaIsNull(UUID id);

    List<ThuocTinh> findByDoiTuongQuanLyIdAndNgayXoaIsNullOrderByTenAsc(UUID doiTuongQuanLyId);

    List<ThuocTinh> findByDoiTuongQuanLyIdInAndNgayXoaIsNullOrderByTenAsc(Collection<UUID> doiTuongQuanLyIds);

    @Query("""
            SELECT t FROM ThuocTinh t
            WHERE (:includeDeleted = TRUE OR t.ngayXoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR t.hoatDong = TRUE)
              AND (:doiTuongQuanLyId IS NULL OR t.doiTuongQuanLyId = :doiTuongQuanLyId)
              AND (
                    :selectorOnly IS NULL OR :selectorOnly = FALSE
                    OR NOT EXISTS (
                        SELECT 1 FROM DoiTuongQuanLy dt
                        WHERE dt.id = t.doiTuongQuanLyId
                          AND dt.hienThiTrenGiaoDien = FALSE
                          AND (:includeDeleted = TRUE OR dt.ngayXoa IS NULL)
                    )
                  )
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(t.ten, ''), ' ',
                        COALESCE(t.kieuDuLieuId, ''), ' ',
                        COALESCE(t.donVi, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY t.ngayTao DESC
            """)
    List<ThuocTinh> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("selectorOnly") Boolean selectorOnly,
            @Param("keyword") String keyword);
}
