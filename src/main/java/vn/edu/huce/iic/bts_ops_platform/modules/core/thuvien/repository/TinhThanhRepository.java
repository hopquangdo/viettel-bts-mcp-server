package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TinhThanhRepository extends JpaRepository<TinhThanh, UUID> {

    List<TinhThanh> findByNgayXoaIsNullOrderByMaAsc();

    Optional<TinhThanh> findByIdAndNgayXoaIsNull(UUID id);

    List<TinhThanh> findAllByOrderByMaAsc();

    @Query("""
            SELECT t FROM TinhThanh t
            WHERE t.ngayXoa IS NULL AND t.laTinhCu = FALSE
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(COALESCE(t.ma, ''), ' ', COALESCE(t.ten, ''))) LIKE CONCAT('%', :keyword, '%')
                  )
              AND (:activeOnly = FALSE OR t.hoatDong = TRUE)
            ORDER BY t.ma ASC
            """)
    List<TinhThanh> searchCatalog(@Param("keyword") String keyword, @Param("activeOnly") boolean activeOnly);
}
