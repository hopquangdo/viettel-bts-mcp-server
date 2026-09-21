package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongDoiTuongGiaTriRepository extends JpaRepository<HopDongDoiTuongGiaTri, UUID> {

    List<HopDongDoiTuongGiaTri> findByNgayXoaIsNull();

    Optional<HopDongDoiTuongGiaTri> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongDoiTuongGiaTri> findByHopDongDoiTuongIdAndNgayXoaIsNull(UUID hopDongDoiTuongId);

    List<HopDongDoiTuongGiaTri> findByHopDongDoiTuongIdInAndNgayXoaIsNull(Collection<UUID> hopDongDoiTuongIds);

    /** Nhẹ — suy UUID nhà thầu từ EAV (thuộc tính nguoi_dung / tên nhà thầu). */
    @Query(value = """
            SELECT g.hop_dong_doi_tuong_id, g.gia_tri
            FROM hop_dong_doi_tuong_gia_tri g
            INNER JOIN hop_dong_doi_tuong d ON d.id = g.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL
            INNER JOIN thuoc_tinh t ON t.id = g.thuoc_tinh_id
                AND t.doi_tuong_quan_ly_id = d.doi_tuong_quan_ly_id
                AND t.ngay_xoa IS NULL
            WHERE g.ngay_xoa IS NULL
              AND g.hop_dong_doi_tuong_id IN (:ids)
              AND g.gia_tri IS NOT NULL
              AND trim(g.gia_tri) <> ''
              AND (
                lower(trim(coalesce(t.kieu_du_lieu_id, ''))) = 'nguoi_dung'
                OR lower(coalesce(t.ten, '')) LIKE '%nha thau%'
                OR lower(coalesce(t.ten, '')) LIKE '%nhà thầu%'
                OR lower(coalesce(t.ten, '')) LIKE '%doi tc%'
                OR lower(coalesce(t.ten, '')) LIKE '%đội tc%'
                OR lower(coalesce(t.ten, '')) LIKE '%contractor%'
              )
            """, nativeQuery = true)
    List<Object[]> findContractorLinkValuesForDoiTuongIds(@Param("ids") Collection<UUID> ids);

    @org.springframework.data.jpa.repository.Query("""
            SELECT LOWER(g.giaTri) FROM HopDongDoiTuongGiaTri g
            JOIN HopDongDoiTuong dt ON g.hopDongDoiTuongId = dt.id
            WHERE dt.hopDongId = :hopDongId
              AND g.thuocTinhId = :thuocTinhId
              AND g.ngayXoa IS NULL
              AND dt.ngayXoa IS NULL
              AND g.giaTri IS NOT NULL
            """)
    List<String> findPrimaryKeyValuesLowerByHopDongAndThuocTinh(
            @org.springframework.data.repository.query.Param("hopDongId") UUID hopDongId,
            @org.springframework.data.repository.query.Param("thuocTinhId") UUID thuocTinhId);

    /** Cặp (giá trị khóa chính lower, id đối tượng) — phục vụ import UPSERT: trùng mã thì cập nhật. */
    @org.springframework.data.jpa.repository.Query("""
            SELECT LOWER(g.giaTri), g.hopDongDoiTuongId FROM HopDongDoiTuongGiaTri g
            JOIN HopDongDoiTuong dt ON g.hopDongDoiTuongId = dt.id
            WHERE dt.hopDongId = :hopDongId
              AND g.thuocTinhId = :thuocTinhId
              AND g.ngayXoa IS NULL
              AND dt.ngayXoa IS NULL
              AND g.giaTri IS NOT NULL
            """)
    List<Object[]> findPrimaryKeyValueIdPairsByHopDongAndThuocTinh(
            @org.springframework.data.repository.query.Param("hopDongId") UUID hopDongId,
            @org.springframework.data.repository.query.Param("thuocTinhId") UUID thuocTinhId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuongGiaTri g
            SET g.ngayXoa = :now, g.hoatDong = false
            WHERE g.hopDongDoiTuongId IN :ids AND g.ngayXoa IS NULL
            """)
    int softDeleteByHopDongDoiTuongIds(
            @Param("ids") Collection<UUID> ids,
            @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE hop_dong_doi_tuong_gia_tri g
            SET ngay_xoa = :now, hoat_dong = false
            FROM hop_dong_doi_tuong d
            WHERE g.hop_dong_doi_tuong_id = d.id
              AND g.ngay_xoa IS NULL
              AND d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR d.hoat_dong = TRUE)
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    int softDeleteByFilter(
            @Param("now") Instant now,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE hop_dong_doi_tuong_gia_tri g
            SET ngay_xoa = :now, hoat_dong = false
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE g.hop_dong_doi_tuong_id = d.id
              AND g.ngay_xoa IS NULL
              AND d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR d.hoat_dong = TRUE)
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri gf
                        WHERE gf.hop_dong_doi_tuong_id = d.id
                          AND gf.ngay_xoa IS NULL
                          AND LOWER(COALESCE(gf.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    int softDeleteByFilterWithKeyword(
            @Param("now") Instant now,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);
}
