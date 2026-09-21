package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.util.Optional;
import java.util.UUID;

@Repository("mcpHopDongRepository")
public interface HopDongRepository extends JpaRepository<HopDong, UUID> {

    @Query(value = """
            SELECT id, ma_hop_dong AS ma, ten
            FROM hop_dong
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND id = :id
            """, nativeQuery = true)
        Optional<HopDongInfoProjection> findInfoById(@Param("id") UUID id);

    /** Bước 1 của resolve: khớp ĐÚNG mã (dùng index sẵn có), không đụng tới tìm gần đúng. */
    @Query(value = """
            SELECT id, ma_hop_dong AS ma, ten FROM hop_dong
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND (ma_hop_dong = :ma OR ma = :ma)
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<HopDongInfoProjection> findByMaExact(@Param("ma") String ma);

    /**
     * Bước 2 (chỉ khi bước 1 không ra): tìm gần đúng, tối đa 5 ứng viên, hạng 1 = khớp đúng, 2 = bắt đầu bằng, 3 = chứa.
     * :k là từ khoá chữ thường, :prefix/:contains đã escape ký tự đại diện của LIKE.
     */
    @Query(value = """
            SELECT id, ma_hop_dong AS ma, ten,
                   CASE WHEN LOWER(ma_hop_dong) = :k OR LOWER(ma) = :k OR LOWER(ten) = :k THEN 1
                        WHEN LOWER(ma_hop_dong) LIKE :prefix ESCAPE '\\' OR LOWER(ma) LIKE :prefix ESCAPE '\\' OR LOWER(ten) LIKE :prefix ESCAPE '\\' THEN 2
                        ELSE 3 END AS hang
            FROM hop_dong
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND (LOWER(ma_hop_dong) LIKE :contains ESCAPE '\\' OR LOWER(ma) LIKE :contains ESCAPE '\\' OR LOWER(ten) LIKE :contains ESCAPE '\\')
            ORDER BY hang, ten
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<UngVien> searchGanDung(@Param("k") String k, @Param("prefix") String prefix, @Param("contains") String contains);

    /** Ứng viên tìm gần đúng kèm hạng khớp. */
    interface UngVien extends HopDongInfoProjection {
        Integer getHang();
    }
}
