package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTru;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongLuuTruRepository extends JpaRepository<HopDongLuuTru, UUID> {

    Optional<HopDongLuuTru> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    List<HopDongLuuTru> findByHopDongIdInAndNgayXoaIsNull(Collection<UUID> hopDongIds);

    @Query("""
            SELECT l.hopDongId FROM HopDongLuuTru l
            WHERE l.ngayXoa IS NULL AND LOWER(l.trangThai) = 'archived'
            """)
    List<UUID> findArchivedHopDongIds();

    @Query(value = """
            SELECT h.loai_hop_dong_id, COUNT(*)
            FROM hop_dong_luu_tru lt
            JOIN hop_dong h ON h.id = lt.hop_dong_id
            WHERE lt.ngay_xoa IS NULL
              AND LOWER(lt.trang_thai) = 'archived'
              AND h.ngay_xoa IS NULL
            GROUP BY h.loai_hop_dong_id
            """, nativeQuery = true)
    List<Object[]> countArchivedGroupByLoaiHopDongId();

    @Query(value = """
            SELECT h.kieu_hop_dong_id, COUNT(*)
            FROM hop_dong_luu_tru lt
            JOIN hop_dong h ON h.id = lt.hop_dong_id
            WHERE lt.ngay_xoa IS NULL
              AND LOWER(lt.trang_thai) = 'archived'
              AND h.ngay_xoa IS NULL
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            GROUP BY h.kieu_hop_dong_id
            """, nativeQuery = true)
    List<Object[]> countArchivedGroupByKieuHopDongId(@Param("loaiHopDongId") UUID loaiHopDongId);

    @Query(value = """
            SELECT d.hop_dong_id,
                   string_agg(DISTINCT kv.ma, '-' ORDER BY kv.ma) AS khu_vuc_ma
            FROM hop_dong_doi_tuong d
            JOIN khu_vuc kv ON kv.id = d.khu_vuc_id AND kv.ngay_xoa IS NULL
            WHERE d.ngay_xoa IS NULL
              AND d.hoat_dong = TRUE
              AND d.hop_dong_id = ANY(CAST(:hopDongIds AS uuid[]))
            GROUP BY d.hop_dong_id
            """, nativeQuery = true)
    List<Object[]> findKhuVucMaByHopDongIds(@Param("hopDongIds") UUID[] hopDongIds);
}
