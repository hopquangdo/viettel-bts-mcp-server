package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.huce.iic.bts_ops_platform.dto.khuvuc.KhuVucInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;

import java.util.Optional;
import java.util.UUID;

/** Repository riêng cho mcp/services/KhuVucService — kiểm tra tồn tại khu vực theo mã hoặc tên. */
@Repository("mcpKhuVucRepository")
public interface KhuVucRepository extends JpaRepository<KhuVuc, UUID> {

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM khu_vuc WHERE ngay_xoa IS NULL AND (LOWER(ma) = LOWER(:keyword) OR LOWER(ten) = LOWER(:keyword)))
            """, nativeQuery = true)
    boolean existsByMaOrTen(@Param("keyword") String keyword);

        @Query(value = """
                        SELECT id, ma, ten FROM khu_vuc
                        WHERE ngay_xoa IS NULL AND id = :id
                        """, nativeQuery = true)
        Optional<KhuVucInfoProjection> findInfoById(@Param("id") UUID id);

    

    /** Bước 1 của resolve: khớp ĐÚNG mã (dùng index sẵn có), không đụng tới tìm gần đúng. */
    @Query(value = """
            SELECT id, ma, ten FROM khu_vuc
            WHERE ngay_xoa IS NULL AND (ma = :ma)
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<KhuVucInfoProjection> findByMaExact(@Param("ma") String ma);

    /**
     * Bước 2 (chỉ khi bước 1 không ra): tìm gần đúng, tối đa 5 ứng viên, hạng 1 = khớp đúng, 2 = bắt đầu bằng, 3 = chứa.
     * :k là từ khoá chữ thường, :prefix/:contains đã escape ký tự đại diện của LIKE.
     */
    @Query(value = """
            SELECT id, ma, ten,
                   CASE WHEN LOWER(ma) = :k OR LOWER(ten) = :k THEN 1
                        WHEN LOWER(ma) LIKE :prefix ESCAPE '\\' OR LOWER(ten) LIKE :prefix ESCAPE '\\' THEN 2
                        ELSE 3 END AS hang
            FROM khu_vuc
            WHERE ngay_xoa IS NULL AND (LOWER(ma) LIKE :contains ESCAPE '\\' OR LOWER(ten) LIKE :contains ESCAPE '\\')
            ORDER BY hang, ten
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<UngVien> searchGanDung(@Param("k") String k, @Param("prefix") String prefix, @Param("contains") String contains);

    /** Ứng viên tìm gần đúng kèm hạng khớp. */
    interface UngVien extends KhuVucInfoProjection {
        Integer getHang();
    }
}
