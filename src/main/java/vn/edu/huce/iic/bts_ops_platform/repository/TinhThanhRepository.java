package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.tinh.TinhThanhInfoProjection;
import org.springframework.stereotype.Repository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;

import java.util.Optional;
import java.util.UUID;

/** Repository riêng cho mcp/services/TinhService — kiểm tra tồn tại tỉnh/thành theo mã, tên hoặc mã tỉnh cũ. */
@Repository("mcpTinhThanhRepository")
public interface TinhThanhRepository extends JpaRepository<TinhThanh, UUID> {

    @Query(value = """
            SELECT EXISTS(
                SELECT 1 FROM tinh_thanh
                WHERE ngay_xoa IS NULL
                  AND (
                    LOWER(ma) = LOWER(:keyword)
                    OR LOWER(ten) = LOWER(:keyword)
                    OR EXISTS (
                        SELECT 1 FROM tinh_thanh legacy
                        WHERE legacy.ngay_xoa IS NULL
                          AND legacy.tinh_thanh_id = tinh_thanh.tinh_thanh_id
                          AND legacy.la_tinh_cu = TRUE
                          AND (LOWER(legacy.ma) = LOWER(:keyword) OR LOWER(legacy.ten) = LOWER(:keyword))
                    )
                  )
            )
            """, nativeQuery = true)
    boolean existsByMaOrTenOrLegacyMa(@Param("keyword") String keyword);

    @Query(value = """
            SELECT id, ma, ten FROM tinh_thanh
            WHERE ngay_xoa IS NULL AND id = :id
            """, nativeQuery = true)
    Optional<TinhThanhInfoProjection> findInfoById(@Param("id") UUID id);

    @Query(value = """
            SELECT id, ma, ten FROM tinh_thanh
            WHERE ngay_xoa IS NULL
                AND (LOWER(ma) = LOWER(:keyword) OR LOWER(ten) = LOWER(:keyword) OR LOWER(ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY CASE WHEN LOWER(ma) = LOWER(:keyword) THEN 0 ELSE 1 END,
                     CASE WHEN LOWER(ten) = LOWER(:keyword) THEN 0 ELSE 1 END,
                     ten
            LIMIT 1
            """, nativeQuery = true)
    Optional<TinhThanhInfoProjection> findInfoByMaOrTen(@Param("keyword") String keyword);

    @Query(value = """
            SELECT current.id, current.ma, current.ten
            FROM tinh_thanh current
            WHERE current.ngay_xoa IS NULL
              AND current.la_tinh_cu = FALSE
              AND current.tinh_thanh_id = (
                  SELECT legacy.tinh_thanh_id
                  FROM tinh_thanh legacy
                  WHERE legacy.ngay_xoa IS NULL
                    AND legacy.la_tinh_cu = TRUE
                    AND (LOWER(legacy.ma) = LOWER(:keyword) OR LOWER(legacy.ten) = LOWER(:keyword))
                  LIMIT 1
              )
            LIMIT 1
            """, nativeQuery = true)
    Optional<TinhThanhInfoProjection> findInfoByLegacyMaOrTen(@Param("keyword") String keyword);

    @Query(value = """
            SELECT legacy.ma
            FROM tinh_thanh legacy
            WHERE legacy.ngay_xoa IS NULL
              AND legacy.la_tinh_cu = TRUE
              AND legacy.tinh_thanh_id = (
                  SELECT current.tinh_thanh_id
                  FROM tinh_thanh current
                  WHERE current.ngay_xoa IS NULL
                    AND current.id = :id
                  LIMIT 1
              )
            ORDER BY legacy.ma
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findLegacyMaByTinhId(@Param("id") UUID id);

}
