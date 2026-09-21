package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuong;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SanLuongRepository extends JpaRepository<SanLuong, UUID> {

    List<SanLuong> findByNgayXoaIsNull();

    long countByNgayXoaIsNull();

    Optional<SanLuong> findByIdAndNgayXoaIsNull(UUID id);


    List<SanLuong> findByHopDongDoiTuongIdAndNgayXoaIsNull(UUID hopDongDoiTuongId);

    List<SanLuong> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    List<SanLuong> findByHopDongDoiTuongIdInAndNgayXoaIsNull(Collection<UUID> hopDongDoiTuongIds);

    @Query("""
            SELECT s FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND s.hopDongDoiTuongId IN :hopDongDoiTuongIds
              AND s.hoatDong = TRUE
              AND (:hasDateFrom = false OR s.ngayThucHien >= :dateFrom)
              AND (:hasDateTo = false OR s.ngayThucHien <= :dateTo)
            """)
    List<SanLuong> findActiveByHopDongDoiTuongIdInAndDateRange(
            @Param("hopDongDoiTuongIds") Collection<UUID> hopDongDoiTuongIds,
            @Param("hasDateFrom") boolean hasDateFrom,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("hasDateTo") boolean hasDateTo,
            @Param("dateTo") LocalDate dateTo);



    @Query("""
            SELECT COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND (s.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND s.ngayThucHien IS NOT NULL
              AND s.ngayThucHien >= :dateFrom
              AND s.ngayThucHien <= :dateTo
            """)
    BigDecimal sumThanhTien(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    /**
     * Cùng điều kiện lọc với sumThanhTien nhưng group theo tháng (date_trunc) — dùng cho
     * xu hướng sản lượng nhiều tháng trong 1 query thay vì lặp gọi sumThanhTien N lần/tháng.
     * row[0] = tháng (java.sql.Timestamp, ngày đầu tháng), row[1] = tổng thành tiền tháng đó.
     */
    @Query(value = """
            SELECT date_trunc('month', s.ngay_thuc_hien) AS thang,
                   COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0) AS tong
            FROM san_luong s
            WHERE s.ngay_xoa IS NULL
              AND (s.hoat_dong = TRUE OR CAST(:activeOnly AS boolean) IS NULL OR CAST(:activeOnly AS boolean) = FALSE)
              AND s.ngay_thuc_hien IS NOT NULL
              AND s.ngay_thuc_hien >= :dateFrom
              AND s.ngay_thuc_hien <= :dateTo
            GROUP BY date_trunc('month', s.ngay_thuc_hien)
            """, nativeQuery = true)
    List<Object[]> sumThanhTienGroupByThang(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT h.loaiHopDongId, COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            JOIN HopDong h ON h.id = s.hopDongId
            WHERE s.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND (s.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND h.loaiHopDongId IS NOT NULL
            GROUP BY h.loaiHopDongId
            """)
    List<Object[]> sumThanhTienGroupByLoaiHopDong(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT d.id, d.hopDongId,
                   COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            JOIN HopDongDoiTuong d ON d.id = s.hopDongDoiTuongId
            JOIN HopDong h ON h.id = d.hopDongId
            WHERE s.ngayXoa IS NULL
              AND d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND (s.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND d.hopDongId IS NOT NULL
            GROUP BY d.id, d.hopDongId
            """)
    List<Object[]> sumThanhTienGroupByHopDongDoiTuong(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT s.hopDongDoiTuongId, s.ngayThucHien,
                   COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND (s.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND s.hopDongDoiTuongId IS NOT NULL
              AND s.ngayThucHien IS NOT NULL
              AND s.ngayThucHien >= :dateFrom
              AND s.ngayThucHien <= :dateTo
            GROUP BY s.hopDongDoiTuongId, s.ngayThucHien
            """)
    List<Object[]> sumThanhTienGroupByHopDongDoiTuongAndNgay(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
    
    @Query("""
            SELECT s.hangMucCongViecId, COALESCE(SUM(s.khoiLuongHoanThanh), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND s.hopDongId = :hopDongId
              AND s.hangMucCongViecId IS NOT NULL
            GROUP BY s.hangMucCongViecId
            """)
    List<Object[]> sumKhoiLuongTheoCongViecByHopDong(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT s.hangMucChiTietId, COALESCE(SUM(s.khoiLuongHoanThanh), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND s.hopDongId = :hopDongId
              AND s.hangMucChiTietId IS NOT NULL
              AND s.hangMucCongViecId IS NULL
            GROUP BY s.hangMucChiTietId
            """)
    List<Object[]> sumKhoiLuongTheoChiTietByHopDong(@Param("hopDongId") UUID hopDongId);

    /** Tổng km khảo sát (trangThai=survey) của hợp đồng — dùng cảnh báo vượt khối lượng HĐ TVTK. */
    @Query("""
            SELECT COALESCE(SUM(s.khoiLuongHoanThanh), 0)
            FROM SanLuong s
            LEFT JOIN HopDongDoiTuong d ON d.id = s.hopDongDoiTuongId AND d.ngayXoa IS NULL
            WHERE s.ngayXoa IS NULL
              AND s.hoatDong = TRUE
              AND COALESCE(s.hopDongId, d.hopDongId) = :hopDongId
              AND LOWER(COALESCE(s.trangThai, '')) = 'survey'
            """)
    BigDecimal sumSurveyKmByHopDong(@Param("hopDongId") UUID hopDongId);

    /** Tổng thành tiền sản lượng của hợp đồng (donGia × khoiLuongHoanThanh). */
    @Query("""
            SELECT COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            LEFT JOIN HopDongDoiTuong d ON d.id = s.hopDongDoiTuongId AND d.ngayXoa IS NULL
            WHERE s.ngayXoa IS NULL
              AND s.hoatDong = TRUE
              AND COALESCE(s.hopDongId, d.hopDongId) = :hopDongId
            """)
    BigDecimal sumThanhTienByHopDong(@Param("hopDongId") UUID hopDongId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SanLuong s
            SET s.ngayXoa = :now, s.hoatDong = false
            WHERE s.hopDongDoiTuongId IN :ids AND s.ngayXoa IS NULL
            """)
    int softDeleteByHopDongDoiTuongIds(@Param("ids") Collection<UUID> ids, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE san_luong s
            SET ngay_xoa = :now, hoat_dong = false
            FROM hop_dong_doi_tuong d
            WHERE s.hop_dong_doi_tuong_id = d.id
              AND s.ngay_xoa IS NULL
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
            UPDATE san_luong s
            SET ngay_xoa = :now, hoat_dong = false
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE s.hop_dong_doi_tuong_id = d.id
              AND s.ngay_xoa IS NULL
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

    @Query("""
            SELECT s.hopDongDoiTuongId, COALESCE(SUM(s.khoiLuongHoanThanh * s.donGia), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND s.hopDongDoiTuongId IN :ids
            GROUP BY s.hopDongDoiTuongId
            """)
    List<Object[]> sumTienTheoDoiTuongIds(@Param("ids") Collection<UUID> ids);

    @Query("""
            SELECT s.hopDongId, COALESCE(SUM(COALESCE(s.donGia, 0) * COALESCE(s.khoiLuongHoanThanh, 0)), 0)
            FROM SanLuong s
            WHERE s.ngayXoa IS NULL
              AND s.hopDongId IS NOT NULL
              AND (s.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND s.ngayThucHien IS NOT NULL
              AND s.ngayThucHien >= :dateFrom
              AND s.ngayThucHien <= :dateTo
            GROUP BY s.hopDongId
            """)
    List<Object[]> sumThanhTienGroupByHopDong(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = """
            SELECT COALESCE(s.hop_dong_id, d.hop_dong_id) AS hop_dong_id,
                   COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0)
            FROM san_luong s
            LEFT JOIN hop_dong_doi_tuong d
                ON d.id = s.hop_dong_doi_tuong_id
               AND d.ngay_xoa IS NULL
            WHERE s.ngay_xoa IS NULL
              AND (s.hoat_dong = TRUE OR CAST(:activeOnly AS boolean) IS NULL OR CAST(:activeOnly AS boolean) = FALSE)
              AND s.ngay_thuc_hien IS NOT NULL
              AND s.ngay_thuc_hien >= :dateFrom
              AND s.ngay_thuc_hien <= :dateTo
              AND COALESCE(s.hop_dong_id, d.hop_dong_id) IS NOT NULL
            GROUP BY COALESCE(s.hop_dong_id, d.hop_dong_id)
            """, nativeQuery = true)
    List<Object[]> sumThanhTienGroupByHopDongResolved(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = """
            SELECT COALESCE(s.hop_dong_id, d.hop_dong_id) AS hop_dong_id,
                   COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0)
            FROM san_luong s
            LEFT JOIN hop_dong_doi_tuong d
                ON d.id = s.hop_dong_doi_tuong_id
               AND d.ngay_xoa IS NULL
            WHERE s.ngay_xoa IS NULL
              AND (s.hoat_dong = TRUE OR CAST(:activeOnly AS boolean) IS NULL OR CAST(:activeOnly AS boolean) = FALSE)
              AND s.ngay_thuc_hien IS NOT NULL
              AND s.ngay_thuc_hien >= :dateFrom
              AND s.ngay_thuc_hien <= :dateTo
              AND COALESCE(s.hop_dong_id, d.hop_dong_id) IS NOT NULL
              AND LOWER(COALESCE(s.trang_thai, '')) IN ('done', 'da_duyet', 'approved', 'hoan_thanh', 'da_tc')
            GROUP BY COALESCE(s.hop_dong_id, d.hop_dong_id)
            """, nativeQuery = true)
    List<Object[]> sumThanhTienDtGroupByHopDongResolved(
            @Param("activeOnly") Boolean activeOnly,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query(value = """
            WITH sl_filtered AS NOT MATERIALIZED (
                SELECT
                    s.hop_dong_doi_tuong_id,
                    s.trang_thai,
                    s.don_gia,
                    s.khoi_luong_hoan_thanh,
                    s.ngay_thuc_hien,
                    s.ngay_cap_nhat,
                    s.hang_muc_cong_viec_id,
                    s.hang_muc_chi_tiet_id
                FROM san_luong s
                INNER JOIN hop_dong_doi_tuong d
                    ON d.id = s.hop_dong_doi_tuong_id
                    AND d.ngay_xoa IS NULL
                    AND d.hoat_dong = TRUE
                INNER JOIN hop_dong h
                    ON h.id = d.hop_dong_id
                    AND h.ngay_xoa IS NULL
                    AND h.hoat_dong = TRUE
                WHERE s.ngay_xoa IS NULL
                  AND s.hoat_dong = TRUE
                  AND (:hasHopDongId = FALSE OR d.hop_dong_id = :hopDongId)
                  AND (:hasDoiTuongQuanLyIds = FALSE OR d.doi_tuong_quan_ly_id IN (:doiTuongQuanLyIds))
                  AND (:hasScope = FALSE OR d.id IN (:scopeIds))
                  AND (:hasDateFrom = FALSE OR s.ngay_thuc_hien >= :dateFrom)
                  AND (:hasDateTo = FALSE OR s.ngay_thuc_hien <= :dateTo)
                  AND (:hasContractorId = FALSE OR d.nha_thau_id = :contractorId)
            ),
            per_doi_tuong AS (
                SELECT
                    s.hop_dong_doi_tuong_id AS doi_tuong_id,
                    SUM(CASE
                        WHEN s.trang_thai = 'done'
                        THEN COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)
                        ELSE 0
                    END) AS productive_total,
                    SUM(CASE
                        WHEN s.trang_thai = 'done'
                         AND (
                                (s.ngay_cap_nhat AT TIME ZONE 'Asia/Ho_Chi_Minh')::date = :today
                                OR s.ngay_thuc_hien = :today
                             )
                        THEN COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)
                        ELSE 0
                    END) AS today_total
                FROM sl_filtered s
                GROUP BY s.hop_dong_doi_tuong_id
            )
            SELECT
                COUNT(*)::int AS totalDisplayed,
                COUNT(*) FILTER (WHERE pd.productive_total > 0)::int AS withOutput,
                COALESCE(SUM(pd.productive_total), 0) AS periodTotal,
                COALESCE(SUM(pd.today_total), 0) AS todayTotal
            FROM per_doi_tuong pd
            """, nativeQuery = true)
    Optional<SanLuongTongHopAggregate> aggregateTongHop(
            @Param("hasHopDongId") boolean hasHopDongId,
            @Param("hopDongId") UUID hopDongId,
            @Param("hasDoiTuongQuanLyIds") boolean hasDoiTuongQuanLyIds,
            @Param("doiTuongQuanLyIds") Collection<UUID> doiTuongQuanLyIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds,
            @Param("hasDateFrom") boolean hasDateFrom,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("hasDateTo") boolean hasDateTo,
            @Param("dateTo") LocalDate dateTo,
            @Param("today") LocalDate today,
            @Param("hasContractorId") boolean hasContractorId,
            @Param("contractorId") UUID contractorId);

    /**
     * Trước đây GROUP BY s.hop_dong_doi_tuong_id + ORDER BY MAX(s.ngay_cap_nhat) trên toàn bộ
     * san_luong khớp filter — phải quét/aggregate hết mới sort+limit được, dù đã có sẵn cột
     * denormalize hop_dong_doi_tuong.ngay_thi_cong_gan_nhat (xem V004 migration) chính là để
     * tránh việc này. Đổi sang EXISTS (semi-join, dùng idx_san_luong_doi_tuong_ngay_thuc_hien)
     * để lọc "đối tượng có sản lượng khớp filter", rồi ORDER BY thẳng cột denormalize đã có
     * index (idx_hop_dong_doi_tuong_ngay_thi_cong_gan_nhat) — Postgres có thể dùng index scan
     * cho ORDER BY + LIMIT thay vì phải gom hết rồi mới sort.
     *
     * <p>Đổi hành vi cần biết: sort key đổi từ "MAX(ngay_cap_nhat) mọi bản ghi active" sang
     * "ngay_thi_cong_gan_nhat" (chỉ tính bản ghi trang_thai='done', xem V004) — đúng ý định gốc
     * của migration ("sort theo ngày thi công thực, không theo timestamp hệ thống"), nhưng thứ
     * tự danh sách có thể đổi cho các đối tượng mà lần cập nhật gần nhất không phải bản ghi done
     * (vd chỉ có issue/survey/design mới nhất).
     */
    @Query(
            value = """
                    SELECT d.id
                    FROM hop_dong_doi_tuong d
                    INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                    WHERE d.ngay_xoa IS NULL
                      AND d.hoat_dong = TRUE
                      AND (:hasHopDongId = FALSE OR d.hop_dong_id = :hopDongId)
                      AND (:hasDoiTuongQuanLyIds = FALSE OR d.doi_tuong_quan_ly_id IN (:doiTuongQuanLyIds))
                      AND (:hasScope = FALSE OR d.id IN (:scopeIds))
                      AND (:hasContractorId = FALSE OR d.nha_thau_id = :contractorId)
                      AND EXISTS (
                            SELECT 1
                            FROM san_luong s
                            WHERE s.hop_dong_doi_tuong_id = d.id
                              AND s.ngay_xoa IS NULL
                              AND s.hoat_dong = TRUE
                              AND (:hasDateFrom = FALSE OR s.ngay_thuc_hien >= :dateFrom)
                              AND (:hasDateTo = FALSE OR s.ngay_thuc_hien <= :dateTo)
                          )
                    ORDER BY d.ngay_thi_cong_gan_nhat DESC NULLS LAST, d.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM hop_dong_doi_tuong d
                    INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                    WHERE d.ngay_xoa IS NULL
                      AND d.hoat_dong = TRUE
                      AND (:hasHopDongId = FALSE OR d.hop_dong_id = :hopDongId)
                      AND (:hasDoiTuongQuanLyIds = FALSE OR d.doi_tuong_quan_ly_id IN (:doiTuongQuanLyIds))
                      AND (:hasScope = FALSE OR d.id IN (:scopeIds))
                      AND (:hasContractorId = FALSE OR d.nha_thau_id = :contractorId)
                      AND EXISTS (
                            SELECT 1
                            FROM san_luong s
                            WHERE s.hop_dong_doi_tuong_id = d.id
                              AND s.ngay_xoa IS NULL
                              AND s.hoat_dong = TRUE
                              AND (:hasDateFrom = FALSE OR s.ngay_thuc_hien >= :dateFrom)
                              AND (:hasDateTo = FALSE OR s.ngay_thuc_hien <= :dateTo)
                          )
                    """,
            nativeQuery = true)
    Page<UUID> findPageRowsForSanLuong(
            @Param("hasHopDongId") boolean hasHopDongId,
            @Param("hopDongId") UUID hopDongId,
            @Param("hasDoiTuongQuanLyIds") boolean hasDoiTuongQuanLyIds,
            @Param("doiTuongQuanLyIds") Collection<UUID> doiTuongQuanLyIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds,
            @Param("hasContractorId") boolean hasContractorId,
            @Param("contractorId") UUID contractorId,
            @Param("hasDateFrom") boolean hasDateFrom,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("hasDateTo") boolean hasDateTo,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    /**
     * Tìm đối tượng theo mã/tên/giá trị thuộc tính — bỏ lọc ngày để luôn tra được mã trạm
     * dù sản lượng nằm ngoài khoảng thời gian đang xem trên UI.
     */
    @Query(
            value = """
                    SELECT d.id
                    FROM hop_dong_doi_tuong d
                    INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                    LEFT JOIN doi_tuong_quan_ly dt
                        ON dt.id = d.doi_tuong_quan_ly_id
                        AND dt.ngay_xoa IS NULL
                    WHERE d.ngay_xoa IS NULL
                      AND d.hoat_dong = TRUE
                      AND (:hasHopDongId = FALSE OR d.hop_dong_id = :hopDongId)
                      AND (:hasDoiTuongQuanLyIds = FALSE OR d.doi_tuong_quan_ly_id IN (:doiTuongQuanLyIds))
                      AND (:hasScope = FALSE OR d.id IN (:scopeIds))
                      AND (:hasContractorId = FALSE OR d.nha_thau_id = :contractorId)
                      AND (
                            :requireOutput = FALSE
                            OR EXISTS (
                                SELECT 1
                                FROM san_luong s
                                WHERE s.hop_dong_doi_tuong_id = d.id
                                  AND s.ngay_xoa IS NULL
                                  AND s.hoat_dong = TRUE
                            )
                          )
                      AND (
                            LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', :keyword, '%')
                            OR EXISTS (
                                SELECT 1
                                FROM hop_dong_doi_tuong_gia_tri gf
                                WHERE gf.hop_dong_doi_tuong_id = d.id
                                  AND gf.ngay_xoa IS NULL
                                  AND LOWER(COALESCE(gf.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                            )
                      )
                    ORDER BY d.ngay_thi_cong_gan_nhat DESC NULLS LAST, d.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM hop_dong_doi_tuong d
                    INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                    LEFT JOIN doi_tuong_quan_ly dt
                        ON dt.id = d.doi_tuong_quan_ly_id
                        AND dt.ngay_xoa IS NULL
                    WHERE d.ngay_xoa IS NULL
                      AND d.hoat_dong = TRUE
                      AND (:hasHopDongId = FALSE OR d.hop_dong_id = :hopDongId)
                      AND (:hasDoiTuongQuanLyIds = FALSE OR d.doi_tuong_quan_ly_id IN (:doiTuongQuanLyIds))
                      AND (:hasScope = FALSE OR d.id IN (:scopeIds))
                      AND (:hasContractorId = FALSE OR d.nha_thau_id = :contractorId)
                      AND (
                            :requireOutput = FALSE
                            OR EXISTS (
                                SELECT 1
                                FROM san_luong s
                                WHERE s.hop_dong_doi_tuong_id = d.id
                                  AND s.ngay_xoa IS NULL
                                  AND s.hoat_dong = TRUE
                            )
                          )
                      AND (
                            LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', :keyword, '%')
                            OR EXISTS (
                                SELECT 1
                                FROM hop_dong_doi_tuong_gia_tri gf
                                WHERE gf.hop_dong_doi_tuong_id = d.id
                                  AND gf.ngay_xoa IS NULL
                                  AND LOWER(COALESCE(gf.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                            )
                      )
                    """,
            nativeQuery = true)
    Page<UUID> findPageRowsForSanLuongByKeyword(
            @Param("hasHopDongId") boolean hasHopDongId,
            @Param("hopDongId") UUID hopDongId,
            @Param("hasDoiTuongQuanLyIds") boolean hasDoiTuongQuanLyIds,
            @Param("doiTuongQuanLyIds") Collection<UUID> doiTuongQuanLyIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds,
            @Param("hasContractorId") boolean hasContractorId,
            @Param("contractorId") UUID contractorId,
            @Param("requireOutput") boolean requireOutput,
            @Param("keyword") String keyword,
            Pageable pageable);
}
