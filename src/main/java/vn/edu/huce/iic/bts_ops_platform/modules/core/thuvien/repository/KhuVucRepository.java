package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KhuVucRepository extends JpaRepository<KhuVuc, UUID> {

    List<KhuVuc> findByNgayXoaIsNull();

    Optional<KhuVuc> findByIdAndNgayXoaIsNull(UUID id);

    Optional<KhuVuc> findByMaIgnoreCase(String ma);

    @Query("""
            SELECT k FROM KhuVuc k
            WHERE k.ngayXoa IS NULL
              AND (LOWER(k.ma) = LOWER(:keyword) OR LOWER(k.ten) LIKE CONCAT('%', LOWER(:keyword), '%'))
            """)
    List<KhuVuc> searchByMaOrTen(@Param("keyword") String keyword);

    List<KhuVuc> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT COUNT(k)
            FROM KhuVuc k
            WHERE k.ngayXoa IS NULL
              AND (k.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
            """)
    long countFiltered(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT k FROM KhuVuc k
            WHERE k.ngayXoa IS NULL
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(COALESCE(k.ma, ''), ' ', COALESCE(k.ten, ''))) LIKE CONCAT('%', :keyword, '%')
                  )
              AND (:activeOnly = FALSE OR k.hoatDong = TRUE)
            ORDER BY k.ma ASC
            """)
    List<KhuVuc> searchCatalog(@Param("keyword") String keyword, @Param("activeOnly") boolean activeOnly);
}
